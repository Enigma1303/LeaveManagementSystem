package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LeaveApprovalService {

    public LeaveStatus resolveApproval(LeaveRequest leave, Employee approver) {

        Employee leaveOwner = leave.getEmployee();
        LeaveStatus currentStatus = leave.getStatus();
        boolean approverIsAdmin = hasRole(approver, "ROLE_ADMIN");
        boolean approverIsManager = hasRole(approver, "ROLE_MANAGER");

        // self approval prevent karo
        if (leave.getEmployee().getId().equals(approver.getId())) {
            log.warn("Self-approval attempt by: {} for leave: {}", approver.getEmail(), leave.getId());
            throw new AccessDeniedException("You cannot approve your own leave request");
        }

        if (currentStatus == LeaveStatus.APPROVED) {
            throw new BusinessRuleException("Leave is already approved");
        }
        if (currentStatus == LeaveStatus.REJECTED || currentStatus == LeaveStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot approve leave with status " + currentStatus);
        }

        // Single level approval
        if (!leave.getIsMultiLevel()) {

            // hierarchy maintain karni he
            if (approverIsManager && !approverIsAdmin) {
                validateIsSubordinate(leave, approver);
            }

            log.info("Single-level approval granted by {} for leave {}", approver.getEmail(), leave.getId());
            return LeaveStatus.APPROVED;
        }

        // Multi level approval

        if (currentStatus == LeaveStatus.PENDING) {
            // Stage 1 — must be approved by manager

            // handling case jab admin leave request karta he
            if (hasRole(leaveOwner, "ROLE_ADMIN")) {
                log.info("Admin leave {} — skipping stage 1, direct approval by {}", leave.getId(), approver.getEmail());
                return LeaveStatus.APPROVED;
            }

            if (hasRole(leaveOwner, "ROLE_MANAGER")) {

                // agar manager ka koi manager nhi he to pass on to ADMIN directly
                if (leaveOwner.getManager() == null) {
                    log.info("Manager {} has no manager — admin {} doing direct approval for leave {}",
                            leaveOwner.getEmail(), approver.getEmail(), leave.getId());
                    if (!approverIsAdmin) {
                        throw new AccessDeniedException(
                                "This manager has no assigned manager. Only admin can approve this leave");
                    }
                    return LeaveStatus.APPROVED;
                }

                // manager's manager approves pehla stage khatam
                if (!leaveOwner.getManager().getId().equals(approver.getId()) && !approverIsAdmin) {
                    throw new AccessDeniedException(
                            "Stage 1 must be approved by the leave owner's direct manager");
                }
                log.info("Stage 1 approved for manager-submitted leave {} by {}", leave.getId(), approver.getEmail());
                return LeaveStatus.MANAGER_APPROVED;
            }

            if (approverIsManager && !approverIsAdmin) {
                validateIsSubordinate(leave, approver);
            }

            log.info("Stage 1 approved for leave {} by manager {}", leave.getId(), approver.getEmail());
            return LeaveStatus.MANAGER_APPROVED;

        } else if (currentStatus == LeaveStatus.MANAGER_APPROVED) {
            // Stage 2 : admin
            if (!approverIsAdmin) {
                log.warn("Non-admin {} tried to do stage 2 approval for leave {}", approver.getEmail(), leave.getId());
                throw new AccessDeniedException("Stage 2 approval requires admin role");
            }

            log.info("Stage 2 approved for leave {} by admin {}", leave.getId(), approver.getEmail());
            return LeaveStatus.APPROVED;

        } else {
            throw new BusinessRuleException("Leave cannot be approved from status " + currentStatus);
        }
    }

    private void validateIsSubordinate(LeaveRequest leave, Employee manager) {
        boolean isSubordinate = leave.getEmployee().getManager() != null &&
                leave.getEmployee().getManager().getId().equals(manager.getId());
        if (!isSubordinate) {
            log.warn("Manager {} tried to approve leave {} of non-subordinate {}",
                    manager.getEmail(), leave.getId(), leave.getEmployee().getEmail());
            throw new AccessDeniedException("You can only approve leaves of employees directly under you");
        }
    }

    private boolean hasRole(Employee user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getName().equals(role));
    }
}
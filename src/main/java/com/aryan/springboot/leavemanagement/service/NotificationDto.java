package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NotificationDto {

    // Leave info
    private final Long leaveId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer requestedUnits;
    private final String reason;
    private final String rejectionReason;
    private final LeaveStatus status;
    private final ApprovalStage approvalStage;
    private final String leaveTypeName;
    private final Boolean isMultiLevel;

    // Employee
    private final Long employeeId;
    private final String employeeName;
    private final String employeeEmail;

    // Manager
    private final Long managerId;
    private final String managerName;
    private final String managerEmail;

    // Admin
    private final Long adminId;
    private final String adminName;
    private final String adminEmail;

    // Balance
    private final Integer remainingBalance;

    public static NotificationDto from(LeaveRequest leave) {

        Employee employee = leave.getEmployee();
        Employee manager = employee.getManager();

        return NotificationDto.builder()
                .leaveId(leave.getId())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .requestedUnits(leave.getRequestedUnits())
                .reason(leave.getReason())
                .rejectionReason(leave.getRejectionReason())
                .status(leave.getStatus())
                .approvalStage(leave.getApprovalStage())
                .leaveTypeName(leave.getLeaveType().getName())
                .isMultiLevel(leave.getIsMultiLevel()) // fixed

                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .employeeEmail(employee.getEmail())

                .managerId(manager != null ? manager.getId() : null)
                .managerName(manager != null ? manager.getName() : null)
                .managerEmail(manager != null ? manager.getEmail() : null)

                .adminId(null)
                .adminName(null)
                .adminEmail(null)

                .remainingBalance(null)
                .build();
    }
}

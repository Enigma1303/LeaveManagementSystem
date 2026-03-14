package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.Session;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class LeaveValidationService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveValidationService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public void validateSubmitRequest(LeaveSubmitRequest request, Employee employee) {

        if (request.getEndDate().isBefore(request.getStartDate())) {
            log.warn("Invalid dates: endDate {} before startDate {} for employee: {}",
                    request.getEndDate(), request.getStartDate(), employee.getEmail());
            throw new BusinessRuleException("End date cannot be before start date");
        }

        // same day SECOND_HALF → FIRST_HALF is invalid
        if (request.getStartDate().isEqual(request.getEndDate())) {
            if (request.getStartSession() == Session.SECOND_HALF
                    && request.getEndSession() == Session.FIRST_HALF) {
                log.warn("Invalid session combination for same day leave by: {}", employee.getEmail());
                throw new BusinessRuleException(
                        "End session cannot be FIRST_HALF when start session is SECOND_HALF on same day");
            }
        }

        // overlapping leave check aur ha rejected leaves ko pehle hata lena
        // Ab list is req since cancelled and rejected dono ko hatana he
        Long overlappingCount = leaveRequestRepository.countOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate(),
                List.of(LeaveStatus.REJECTED, LeaveStatus.CANCELLED));
        if (overlappingCount > 0) {
            log.warn("Overlapping leave for employee: {} between {} and {}",
                    employee.getEmail(), request.getStartDate(), request.getEndDate());
            throw new BusinessRuleException(
                    "You already have a leave request overlapping with the selected dates");
        }
    }

    public void validateCancelRequest(LeaveRequest leave, Employee requestor) {

        // sirf khudki leave cancel kar sakte he
        if (!leave.getEmployee().getId().equals(requestor.getId())) {
            log.warn("Employee {} tried to cancel leave {} owned by {}",
                    requestor.getEmail(), leave.getId(), leave.getEmployee().getEmail());
            throw new AccessDeniedException("You can only cancel your own leave requests");
        }

        LeaveStatus status = leave.getStatus();

        if (status == LeaveStatus.CANCELLED) {
            throw new BusinessRuleException("Leave is already cancelled");
        }
        if (status == LeaveStatus.REJECTED) {
            throw new BusinessRuleException("Cannot cancel a rejected leave request");
        }

        LocalDate today = LocalDate.now();

        // leave toh start ho gyi he cancel nhi hogi
        if (!today.isBefore(leave.getStartDate())) {
            log.warn("Employee {} tried to cancel leave {} that has already started on {}",
                    requestor.getEmail(), leave.getId(), leave.getStartDate());
            throw new BusinessRuleException(
                    "Cannot cancel a leave that has already started on " + leave.getStartDate());
        }

        // leave toh already le li ab cancel nhi kar sakte
        if (today.isAfter(leave.getEndDate())) {
            log.warn("Employee {} tried to cancel leave {} that has already ended on {}",
                    requestor.getEmail(), leave.getId(), leave.getEndDate());
            throw new BusinessRuleException(
                    "Cannot cancel a leave that has already ended on " + leave.getEndDate());
        }
    }


    // already approved/rejected/cancelled he toh duplicate calls mat karo
    public void validateRejectRequest(LeaveRequest leave) {
        LeaveStatus status = leave.getStatus();
        if (status == LeaveStatus.APPROVED || status == LeaveStatus.REJECTED
                || status == LeaveStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cannot reject leave with status " + status +
                            ". Only PENDING or MANAGER_APPROVED leaves can be rejected");
        }
    }
}
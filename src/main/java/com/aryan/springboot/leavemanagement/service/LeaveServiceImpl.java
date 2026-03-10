package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.LeaveStatusHistory;
import com.aryan.springboot.leavemanagement.entity.LeaveType;
import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.Session;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import com.aryan.springboot.leavemanagement.exception.ResourceNotFoundException;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.LeaveStatusHistoryRepository;
import com.aryan.springboot.leavemanagement.repository.LeaveTypeRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveActionRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveHistoryResponse;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveStatusHistoryRepository leaveStatusHistoryRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final UserRepository userRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            LeaveStatusHistoryRepository leaveStatusHistoryRepository,
                            LeaveTypeRepository leaveTypeRepository,
                            LeaveBalanceService leaveBalanceService,
                            UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveStatusHistoryRepository = leaveStatusHistoryRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceService = leaveBalanceService;
        this.userRepository = userRepository;
    }

    private Employee getUser(String email) {
        return userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private boolean hasRole(Employee user, String role) {
        return user.getAuthorities().stream().anyMatch(a -> a.getName().equals(role));
    }

    private LeaveRequest getLeave(Long leaveId) {
        return leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found for id: " + leaveId));
    }

    private void writeHistory(LeaveRequest leave, LeaveStatus oldStatus,
                              LeaveStatus newStatus, String comment, Employee changedBy) {
        LeaveStatusHistory history = new LeaveStatusHistory();
        history.setLeaveRequest(leave);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setComment(comment);
        history.setChangedBy(changedBy);
        leaveStatusHistoryRepository.save(history);
    }

    private void checkIsSubordinate(Employee manager, LeaveRequest leave) {
        if (leave.getEmployee().getManager() == null) {
            throw new AccessDeniedException(
                    "This employee has no manager assigned. Only Admin can approve this leave.");
        }
        if (!leave.getEmployee().getManager().getId().equals(manager.getId())) {
            throw new AccessDeniedException("You can only act on leaves of your subordinates");
        }
    }

    private int computeRequestedUnits(LocalDate startDate, LocalDate endDate,
                                      Session startSession, Session endSession) {
        if (startDate.isEqual(endDate)) {
            return 1;
        }
        int units = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            java.time.DayOfWeek dow = current.getDayOfWeek();
            boolean isWeekend = (dow == java.time.DayOfWeek.SATURDAY
                    || dow == java.time.DayOfWeek.SUNDAY);
            if (!isWeekend) {
                units += 2;
            }
            current = current.plusDays(1);
        }
        return (int) Math.ceil(units / 2.0);
    }

    // Submit Leave

    @Transactional
    @Override
    public LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email) {
        log.info("Leave submission requested by: {}", email);
        Employee employee = getUser(email);

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave type not found for id: " + request.getLeaveTypeId()));

        if (!leaveType.getIsActive()) {
            throw new BusinessRuleException(
                    "Leave type '" + leaveType.getName() + "' is not active");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessRuleException("End date cannot be before start date");
        }

        if (request.getStartDate().isEqual(request.getEndDate())) {
            if (request.getStartSession() == Session.SECOND_HALF
                    && request.getEndSession() == Session.FIRST_HALF) {
                throw new BusinessRuleException(
                        "End session cannot be FIRST_HALF when start session is SECOND_HALF on same day");
            }
        }

        long daysUntilStart = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), request.getStartDate());
        if (daysUntilStart < leaveType.getMinAdvanceNoticeDays()) {
            throw new BusinessRuleException(
                    "Leave must be submitted at least " + leaveType.getMinAdvanceNoticeDays()
                            + " day(s) in advance. You submitted " + daysUntilStart
                            + " day(s) before start date.");
        }

        int requestedUnits = computeRequestedUnits(
                request.getStartDate(), request.getEndDate(),
                request.getStartSession(), request.getEndSession());

        if (requestedUnits > leaveType.getMaxUnitsPerRequest()) {
            throw new BusinessRuleException(
                    "Requested units (" + requestedUnits + ") exceed maximum allowed ("
                            + leaveType.getMaxUnitsPerRequest() + ") for '"
                            + leaveType.getName() + "'");
        }

        Long overlappingCount = leaveRequestRepository.countOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate(),
                LeaveStatus.REJECTED);
        if (overlappingCount > 0) {
            throw new BusinessRuleException(
                    "You already have a leave request overlapping with selected dates");
        }

        int year = request.getStartDate().getYear();
        leaveBalanceService.checkAvailableBalance(
                employee.getId(), leaveType.getId(), year, requestedUnits);
        leaveBalanceService.lockPendingUnits(
                employee.getId(), leaveType.getId(), year, requestedUnits);

        // Determine if this specific leave requires multi-level approval
        boolean requiresMultiLevel = false;
        if (Boolean.TRUE.equals(leaveType.getIsMultiLevelApproval())) {
            requiresMultiLevel = requestedUnits >= leaveType.getMultiLevelTriggerUnits();
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(leaveType);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStartSession(request.getStartSession());
        leave.setEndSession(request.getEndSession());
        leave.setRequestedUnits(requestedUnits);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setApprovalStage(ApprovalStage.MANAGER);
        leave.setIsMultiLevel(requiresMultiLevel);

        LeaveRequest saved = leaveRequestRepository.save(leave);
        writeHistory(saved, null, LeaveStatus.PENDING, "Leave submitted", employee);

        log.info("Leave submitted id:{} by:{} units:{} multiLevel:{}",
                saved.getId(), email, requestedUnits, requiresMultiLevel);
        return new LeaveSubmitResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt());
    }

    // Get Leaves

    @Transactional(readOnly = true)
    @Override
    public List<LeaveViewResponse> getLeaves(String email, LeaveStatus status, Long employeeId,
                                             Long managerId, LocalDate startDate, LocalDate endDate,
                                             LocalDateTime createdAt, String search) {

        log.info("Fetching leaves for: {}", email);
        Employee user = getUser(email);
        List<LeaveRequest> leaves = new ArrayList<>();

        if (hasRole(user, "ROLE_ADMIN")) {
            leaves = leaveRequestRepository.findAllWithFilters(
                    status, employeeId, managerId, startDate, endDate, createdAt, search);
        } else if (hasRole(user, "ROLE_MANAGER")) {
            leaves.addAll(leaveRequestRepository.findByEmployeeIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search));
            leaves.addAll(leaveRequestRepository.findByManagerIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search));
            leaves.sort(Comparator.comparing(LeaveRequest::getCreatedAt).reversed());
        } else {
            leaves = leaveRequestRepository.findByEmployeeIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search);
        }

        return leaves.stream().map(leave -> new LeaveViewResponse(
                        leave.getId(),
                        leave.getEmployee().getId(),
                        leave.getEmployee().getName(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getStartSession(),
                        leave.getEndSession(),
                        leave.getReason(),
                        leave.getStatus(),
                        leave.getCreatedAt(),
                        leave.getStatusHistory().stream()
                                .sorted(Comparator.comparing(LeaveStatusHistory::getCreatedAt).reversed())
                                .map(h -> new LeaveHistoryResponse(
                                        h.getOldStatus(),
                                        h.getNewStatus(),
                                        h.getComment(),
                                        h.getChangedBy().getName(),
                                        h.getCreatedAt()))
                                .toList()))
                .collect(Collectors.toList());
    }

    // Approve Leave

    @Transactional
    @Override
    public LeaveStatusResponse approveLeave(Long leaveId, LeaveActionRequest request, String email) {
        log.info("Approve requested for leaveId:{} by:{}", leaveId, email);
        Employee user = getUser(email);
        LeaveRequest leave = getLeave(leaveId);
        boolean isMultiLevel = Boolean.TRUE.equals(leave.getIsMultiLevel());

        if (hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            checkIsSubordinate(user, leave);

            if (leave.getStatus() != LeaveStatus.PENDING) {
                throw new BusinessRuleException("Manager can only approve PENDING leaves");
            }
            if (isMultiLevel) {
                // Multi level → manager sets MANAGER_APPROVED, not final APPROVED
                leave.setStatus(LeaveStatus.MANAGER_APPROVED);
                leave.setApprovalStage(ApprovalStage.ADMIN);
                LeaveRequest saved = leaveRequestRepository.save(leave);
                writeHistory(saved, LeaveStatus.PENDING, LeaveStatus.MANAGER_APPROVED,
                        request.getComment(), user);
                log.info("Leave {} manager approved, awaiting admin", leaveId);
                return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
            } else {
                // Single level → manager directly approves
                leave.setStatus(LeaveStatus.APPROVED);
                leave.setApprovalStage(ApprovalStage.COMPLETED);
                LeaveRequest saved = leaveRequestRepository.save(leave);
                deductBalance(leave);
                writeHistory(saved, LeaveStatus.PENDING, LeaveStatus.APPROVED,
                        request.getComment(), user);
                log.info("Leave {} approved by manager (single level)", leaveId);
                return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
            }
        }

        if (hasRole(user, "ROLE_ADMIN")) {
            if (isMultiLevel && leave.getStatus() == LeaveStatus.PENDING) {
                throw new AccessDeniedException(
                        "Manager must approve first for multi-level leaves");
            }
            if (leave.getStatus() != LeaveStatus.PENDING
                    && leave.getStatus() != LeaveStatus.MANAGER_APPROVED) {
                throw new BusinessRuleException(
                        "Leave cannot be approved from status: " + leave.getStatus());
            }
            LeaveStatus oldStatus = leave.getStatus();
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setApprovalStage(ApprovalStage.COMPLETED);
            LeaveRequest saved = leaveRequestRepository.save(leave);
            deductBalance(leave);
            writeHistory(saved, oldStatus, LeaveStatus.APPROVED, request.getComment(), user);
            log.info("Leave {} approved by admin", leaveId);
            return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
        }

        throw new AccessDeniedException("Only Manager or Admin can approve leaves");
    }

    // Reject Leave

    @Transactional
    @Override
    public LeaveStatusResponse rejectLeave(Long leaveId, LeaveActionRequest request, String email) {
        log.info("Reject requested for leaveId:{} by:{}", leaveId, email);
        Employee user = getUser(email);
        LeaveRequest leave = getLeave(leaveId);

        if (!hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only Manager or Admin can reject leaves");
        }

        if (hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            checkIsSubordinate(user, leave);
        }

        if (leave.getStatus() == LeaveStatus.APPROVED
                || leave.getStatus() == LeaveStatus.REJECTED
                || leave.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cannot reject leave with status: " + leave.getStatus());
        }

        LeaveStatus oldStatus = leave.getStatus();
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(request.getComment());
        leave.setApprovalStage(ApprovalStage.COMPLETED);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        releaseBalance(leave);
        writeHistory(saved, oldStatus, LeaveStatus.REJECTED, request.getComment(), user);
        log.info("Leave {} rejected by:{}", leaveId, email);
        return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
    }

    // Cancel Leave

    @Transactional
    @Override
    public LeaveStatusResponse cancelLeave(Long leaveId, LeaveActionRequest request, String email) {
        log.info("Cancel requested for leaveId:{} by:{}", leaveId, email);
        Employee user = getUser(email);
        LeaveRequest leave = getLeave(leaveId);

        // Only the employee who submitted can cancel
        if (!leave.getEmployee().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only cancel your own leave requests");
        }

        LeaveStatus currentStatus = leave.getStatus();

        if (currentStatus == LeaveStatus.REJECTED || currentStatus == LeaveStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Cannot cancel leave with status: " + currentStatus);
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setApprovalStage(ApprovalStage.COMPLETED);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        // Balance restoration depends on previous status
        if (currentStatus == LeaveStatus.APPROVED) {
            // Units were moved to usedUnits on approval → restore them back
            leaveBalanceService.restoreUsedUnits(
                    leave.getEmployee().getId(),
                    leave.getLeaveType().getId(),
                    leave.getStartDate().getYear(),
                    leave.getRequestedUnits());
        } else {
            // PENDING or MANAGER_APPROVED → units are in pendingUnits → release them
            releaseBalance(leave);
        }

        writeHistory(saved, currentStatus, LeaveStatus.CANCELLED, request.getComment(), user);
        log.info("Leave {} cancelled by employee:{}", leaveId, email);
        return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
    }

    // Balance Helpers

    private void deductBalance(LeaveRequest leave) {
        leaveBalanceService.deductOnApproval(
                leave.getEmployee().getId(),
                leave.getLeaveType().getId(),
                leave.getStartDate().getYear(),
                leave.getRequestedUnits());
    }

    private void releaseBalance(LeaveRequest leave) {
        leaveBalanceService.releasePendingUnits(
                leave.getEmployee().getId(),
                leave.getLeaveType().getId(),
                leave.getStartDate().getYear(),
                leave.getRequestedUnits());
    }


}
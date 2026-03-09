package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.LeaveStatusHistory;
import com.aryan.springboot.leavemanagement.entity.LeaveType;
import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.Session;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.LeaveStatusHistoryRepository;
import com.aryan.springboot.leavemanagement.repository.LeaveTypeRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveStatusRequest;
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
import java.util.Map;
import java.util.Set;

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
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new RuntimeException("User not found: " + email);
                });
    }

    private boolean hasRole(Employee user, String role) {
        return user.getAuthorities().stream().anyMatch(a -> a.getName().equals(role));
    }

    private int computeRequestedUnits(LocalDate startDate, LocalDate endDate,
                                      Session startSession, Session endSession) {

        if (startDate.isEqual(endDate)) {
            if (startSession == Session.FIRST_HALF && endSession == Session.SECOND_HALF) {
                return 1;
            }
            return 1;
        }

        int units = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            java.time.DayOfWeek dow = current.getDayOfWeek();
            boolean isWeekend = (dow == java.time.DayOfWeek.SATURDAY
                    || dow == java.time.DayOfWeek.SUNDAY);

            if (!isWeekend) {
                if (current.isEqual(startDate)) {
                    units += 1;
                } else if (current.isEqual(endDate)) {
                    units += 1;
                } else {
                    units += 2;
                }
            }
            current = current.plusDays(1);
        }

        return (int) Math.ceil(units / 2.0);
    }

    @Transactional
    @Override
    public LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email) {
        log.info("Leave submission requested by: {}", email);
        Employee employee = getUser(email);

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException(
                        "Leave type not found for id: " + request.getLeaveTypeId()));

        if (!leaveType.getIsActive()) {
            throw new RuntimeException("Leave type '" + leaveType.getName() + "' is not active");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }
        if (request.getStartDate().isEqual(request.getEndDate())) {
            if (request.getStartSession() == Session.SECOND_HALF
                    && request.getEndSession() == Session.FIRST_HALF) {
                throw new RuntimeException(
                        "End session cannot be FIRST_HALF when start session is SECOND_HALF on the same day");
            }
        }

        long daysUntilStart = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), request.getStartDate());
        if (daysUntilStart < leaveType.getMinAdvanceNoticeDays()) {
            log.warn("Advance notice check failed for user: {} - required {} days, submitted {} days before",
                    email, leaveType.getMinAdvanceNoticeDays(), daysUntilStart);
            throw new RuntimeException(
                    "Leave request must be submitted at least "
                            + leaveType.getMinAdvanceNoticeDays()
                            + " day(s) in advance. You submitted "
                            + daysUntilStart + " day(s) before the start date.");
        }

        int requestedUnits = computeRequestedUnits(
                request.getStartDate(), request.getEndDate(),
                request.getStartSession(), request.getEndSession());

        if (requestedUnits > leaveType.getMaxUnitsPerRequest()) {
            throw new RuntimeException(
                    "Requested units (" + requestedUnits + ") exceed the maximum allowed ("
                            + leaveType.getMaxUnitsPerRequest() + ") for leave type '"
                            + leaveType.getName() + "'");
        }

        Long overlappingCount = leaveRequestRepository.countOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate(),
                LeaveStatus.REJECTED);
        if (overlappingCount > 0) {
            throw new RuntimeException(
                    "You already have a leave request overlapping with the selected dates");
        }

        int year = request.getStartDate().getYear();
        leaveBalanceService.checkAvailableBalance(
                employee.getId(), leaveType.getId(), year, requestedUnits);
        leaveBalanceService.lockPendingUnits(
                employee.getId(), leaveType.getId(), year, requestedUnits);

        boolean isMultiLevel = Boolean.TRUE.equals(leaveType.getIsMultiLevelApproval());

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
        leave.setIsMultiLevel(isMultiLevel);

        LeaveRequest saved = leaveRequestRepository.save(leave);

        LeaveStatusHistory history = new LeaveStatusHistory();
        history.setLeaveRequest(saved);
        history.setOldStatus(null);
        history.setNewStatus(LeaveStatus.PENDING);
        history.setChangedBy(employee);
        history.setComment("Leave submitted");
        leaveStatusHistoryRepository.save(history);

        log.info("Leave submitted successfully - id: {} by: {} units: {}", saved.getId(), email, requestedUnits);
        return new LeaveSubmitResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt());
    }

    @Override
    public List<LeaveViewResponse> getLeaves(String email, LeaveStatus status, Long employeeId,
                                             Long managerId, LocalDate startDate, LocalDate endDate,
                                             LocalDateTime createdAt, String search) {

        log.info("Fetching leaves for: {} with filters", email);
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
        } else if (hasRole(user, "ROLE_EMPLOYEE")) {
            leaves = leaveRequestRepository.findByEmployeeIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search);
        } else {
            throw new AccessDeniedException("User does not have a valid role to view leaves");
        }

        List<LeaveViewResponse> result = leaves.stream().map(leave -> new LeaveViewResponse(
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
                .collect(java.util.stream.Collectors.toList());
        return result;
    }

    @Transactional
    @Override
    public LeaveStatusResponse updateLeaveStatus(Long leaveId, LeaveStatusRequest request, String email) {
        log.info("Leave status update requested for leaveId: {} by: {}", leaveId, email);

        Employee user = getUser(email);

        if (!hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only managers and admins can update leave statuses");
        }

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException(
                        "No such Leave Request found in the database"));

        if (hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            // If the leave belongs to an employee with no manager, only admin can approve
            if (leave.getEmployee().getManager() == null) {
                throw new AccessDeniedException(
                        "This employee has no manager assigned. Only Admin can approve this leave.");
            }
            boolean isSubordinate = leave.getEmployee().getManager().getId().equals(user.getId());
            if (!isSubordinate) {
                throw new AccessDeniedException("You can only update leaves of employees under you");
            }
        }

        LeaveStatus currentStatus = leave.getStatus();
        LeaveStatus newStatus = request.getStatus();

        Map<LeaveStatus, Set<LeaveStatus>> validTransitions = Map.of(
                LeaveStatus.PENDING,
                Set.of(LeaveStatus.MANAGER_APPROVED, LeaveStatus.REJECTED),
                LeaveStatus.MANAGER_APPROVED,
                Set.of(LeaveStatus.APPROVED, LeaveStatus.REJECTED));

        Set<LeaveStatus> allowed = validTransitions.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException(
                    "Cannot transition leave from " + currentStatus + " to " + newStatus);
        }

        if (hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            if (currentStatus != LeaveStatus.PENDING) {
                throw new AccessDeniedException(
                        "Managers can only act on PENDING leaves");
            }
        }

        if (hasRole(user, "ROLE_ADMIN") && !hasRole(user, "ROLE_MANAGER")) {
            if (Boolean.TRUE.equals(leave.getIsMultiLevel())
                    && currentStatus == LeaveStatus.PENDING) {
                throw new AccessDeniedException(
                        "Admin can only act after Manager has approved for multi-level leaves");
            }
        }

        leave.setStatus(newStatus);

        if (newStatus == LeaveStatus.MANAGER_APPROVED) {
            leave.setApprovalStage(ApprovalStage.ADMIN);
        } else if (newStatus == LeaveStatus.APPROVED || newStatus == LeaveStatus.REJECTED) {
            leave.setApprovalStage(ApprovalStage.COMPLETED);
        }

        LeaveRequest saved = leaveRequestRepository.save(leave);

        int year = leave.getStartDate().getYear();
        Long empId = leave.getEmployee().getId();
        Long ltId = leave.getLeaveType().getId();
        int units = leave.getRequestedUnits();

        if (newStatus == LeaveStatus.APPROVED) {
            leaveBalanceService.deductOnApproval(empId, ltId, year, units);
            log.info("Balance deducted on approval for leaveId: {}", leaveId);
        } else if (newStatus == LeaveStatus.REJECTED) {
            leaveBalanceService.releasePendingUnits(empId, ltId, year, units);
            log.info("Balance released on rejection for leaveId: {}", leaveId);
        }

        LeaveStatusHistory history = new LeaveStatusHistory();
        history.setLeaveRequest(saved);
        history.setOldStatus(currentStatus);
        history.setNewStatus(newStatus);
        history.setComment(request.getComment());
        history.setChangedBy(user);
        leaveStatusHistoryRepository.save(history);

        log.info("Leave {} status updated from {} to {} by: {}", leaveId, currentStatus, newStatus, email);
        return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
    }
}
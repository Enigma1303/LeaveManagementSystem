package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.LeaveBalance;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.LeaveStatusHistory;
import com.aryan.springboot.leavemanagement.entity.LeaveType;
import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.Session;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import com.aryan.springboot.leavemanagement.repository.specification.LeaveRequestSpecification;
import com.aryan.springboot.leavemanagement.exception.ResourceNotFoundException;
import com.aryan.springboot.leavemanagement.repository.LeaveBalanceRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveStatusHistoryRepository leaveStatusHistoryRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final HolidayService holidayService;
    private final WorkingDayService workingDayService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LeaveValidationService leaveValidationService;
    private final LeaveApprovalService leaveApprovalService;

    @Value("${app.holiday.country-code}")
    private String countryCode;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            LeaveStatusHistoryRepository leaveStatusHistoryRepository,
                            LeaveTypeRepository leaveTypeRepository,
                            LeaveBalanceService leaveBalanceService,
                            LeaveBalanceRepository leaveBalanceRepository,
                            HolidayService holidayService,
                            WorkingDayService workingDayService,
                            UserRepository userRepository,
                            NotificationService notificationService,
                            LeaveValidationService leaveValidationService,
                            LeaveApprovalService leaveApprovalService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveStatusHistoryRepository = leaveStatusHistoryRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceService = leaveBalanceService;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.holidayService = holidayService;
        this.workingDayService = workingDayService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.leaveValidationService = leaveValidationService;
        this.leaveApprovalService = leaveApprovalService;
    }

    private Employee getUser(String email) {
        return userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private boolean hasRole(Employee user, String role) {
        return user.getAuthorities().stream().anyMatch(a -> a.getName().equals(role));
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

    // Leave request se NotificationDTO build karlo taaki
    // status change pe kaam aaye notification service me pass karne ke liye
    private NotificationDto buildDto(LeaveRequest leave) {
        Employee emp     = leave.getEmployee();
        Employee manager = emp.getManager();
        Employee admin   = userRepository.findByRole("ROLE_ADMIN").orElse(null);

        Integer remaining = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        emp.getId(),
                        leave.getLeaveType().getId(),
                        leave.getStartDate().getYear())
                .map(b -> b.getAllocatedUnits() - b.getUsedUnits() - b.getPendingUnits())
                .orElse(null);

        return new NotificationDto(
                leave.getId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getRequestedUnits(),
                leave.getReason(),
                leave.getRejectionReason(),
                leave.getStatus(),
                leave.getApprovalStage(),
                leave.getLeaveType().getName(),
                emp.getId(),
                emp.getName(),
                emp.getEmail(),
                manager != null ? manager.getId()    : null,
                manager != null ? manager.getName()  : null,
                manager != null ? manager.getEmail() : null,
                admin   != null ? admin.getId()      : null,
                admin   != null ? admin.getName()    : null,
                admin   != null ? admin.getEmail()   : null,
                remaining
        );
    }

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

    private LeaveRequest getLeave(Long leaveId) {
        return leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found for id: " + leaveId));
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

        // delegate karo date/session/overlap validation -> LeaveValidationService
        leaveValidationService.validateSubmitRequest(request, employee);

        long daysUntilStart = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), request.getStartDate());
        if (daysUntilStart < leaveType.getMinAdvanceNoticeDays()) {
            throw new BusinessRuleException(
                    "Leave must be submitted at least " + leaveType.getMinAdvanceNoticeDays()
                            + " day(s) in advance. You submitted " + daysUntilStart
                            + " day(s) before start date.");
        }

        Set<LocalDate> publicHolidays = holidayService.getPublicHolidays(
                request.getStartDate().getYear(), countryCode);

        if (publicHolidays.contains(request.getStartDate())) {
            throw new BusinessRuleException(
                    "Start date " + request.getStartDate() + " is a public holiday");
        }
        if (publicHolidays.contains(request.getEndDate())) {
            throw new BusinessRuleException(
                    "End date " + request.getEndDate() + " is a public holiday");
        }

        int requestedUnits = workingDayService.calculateWorkingDays(
                request.getStartDate(), request.getEndDate(),
                request.getStartSession(), request.getEndSession(), publicHolidays);

        if (requestedUnits > leaveType.getMaxUnitsPerRequest()) {
            throw new BusinessRuleException(
                    "Requested units (" + requestedUnits + ") exceed maximum allowed ("
                            + leaveType.getMaxUnitsPerRequest() + ") for '"
                            + leaveType.getName() + "'");
        }

        int year = request.getStartDate().getYear();
        leaveBalanceService.checkAvailableBalance(
                employee.getId(), leaveType.getId(), year, requestedUnits);
        leaveBalanceService.lockPendingUnits(
                employee.getId(), leaveType.getId(), year, requestedUnits);

        // snapshot now so incase leave-type changes during processing then it doesnt affect flow
        boolean requiresMultiLevel = false;
        if (Boolean.TRUE.equals(leaveType.getIsMultiLevelApproval())) {
            requiresMultiLevel = requestedUnits >= leaveType.getMultiLevelTriggerUnits();
        }

        boolean isAdmin = hasRole(employee, "ROLE_ADMIN");
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

        if (isAdmin) {
            // auto approve — no notification needed, no pending state
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setApprovalStage(ApprovalStage.COMPLETED);
        } else {
            leave.setStatus(LeaveStatus.PENDING);
            leave.setApprovalStage(ApprovalStage.MANAGER);
        }

        LeaveRequest saved = leaveRequestRepository.save(leave);

        if (isAdmin) {
            leaveBalanceService.releasePendingUnits(employee.getId(), leaveType.getId(), year, requestedUnits);
            deductBalance(saved);
            writeHistory(saved, null, LeaveStatus.APPROVED, "Auto approved — admin leave", employee);
            log.info("Admin leave auto-approved id:{} by:{} units:{}", saved.getId(), email, requestedUnits);
        } else {
            writeHistory(saved, null, LeaveStatus.PENDING, "Leave submitted", employee);

            NotificationDto dto = buildDto(saved);
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            notificationService.notifyLeaveSubmitted(dto);
                        }
                    }
            );
            log.info("Leave submitted id:{} by:{} units:{} multiLevel:{}",
                    saved.getId(), email, requestedUnits, requiresMultiLevel);
        }

        return new LeaveSubmitResponse(saved.getId(), saved.getStatus(), saved.getCreatedAt());
    }

    // Get Leaves

    @Transactional(readOnly = true)
    @Override
    public Page<LeaveViewResponse> getLeaves(
            String email,
            LeaveStatus status,
            Long employeeId,
            Long managerId,
            Long leaveTypeId,
            Integer minDuration,
            Integer maxDuration,
            Boolean multiLevel,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        log.info("Fetching leaves for: {}", email);
        Employee user = getUser(email);

        Specification<LeaveRequest> spec = null;

        if (status != null) {
            spec = LeaveRequestSpecification.status(status);
        }
        if (startDate != null) {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.startDate(startDate);
            spec = spec == null ? condition : spec.and(condition);
        }
        if (endDate != null) {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.endDate(endDate);
            spec = spec == null ? condition : spec.and(condition);
        }
        if (leaveTypeId != null) {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.leaveType(leaveTypeId);
            spec = spec == null ? condition : spec.and(condition);
        }
        if (minDuration != null) {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.minDuration(minDuration);
            spec = spec == null ? condition : spec.and(condition);
        }
        if (maxDuration != null) {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.maxDuration(maxDuration);
            spec = spec == null ? condition : spec.and(condition);
        }
        if (multiLevel != null) {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.multiLevel(multiLevel);
            spec = spec == null ? condition : spec.and(condition);
        }

        if (hasRole(user, "ROLE_ADMIN")) {
            if (employeeId != null) {
                Specification<LeaveRequest> condition = LeaveRequestSpecification.employee(employeeId);
                spec = spec == null ? condition : spec.and(condition);
            }
            if (managerId != null) {
                Specification<LeaveRequest> condition = LeaveRequestSpecification.manager(managerId);
                spec = spec == null ? condition : spec.and(condition);
            }
        } else if (hasRole(user, "ROLE_MANAGER")) {
            Specification<LeaveRequest> condition =
                    LeaveRequestSpecification.manager(user.getId())
                            .or(LeaveRequestSpecification.employee(user.getId()));
            spec = spec == null ? condition : spec.and(condition);
        } else {
            Specification<LeaveRequest> condition = LeaveRequestSpecification.employee(user.getId());
            spec = spec == null ? condition : spec.and(condition);
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequest> leavePage = leaveRequestRepository.findAll(spec, pageable);

        return leavePage.map(leave -> new LeaveViewResponse(
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
                        .toList()
        ));
    }

    // Approve Leave

    @Transactional
    @Override
    public LeaveStatusResponse approveLeave(Long leaveId, LeaveActionRequest request, String email) {
        log.info("Approve requested for leaveId:{} by:{}", leaveId, email);
        Employee approver = getUser(email);

        if (!hasRole(approver, "ROLE_MANAGER") && !hasRole(approver, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only Manager or Admin can approve leaves");
        }

        LeaveRequest leave = getLeave(leaveId);
        LeaveStatus oldStatus = leave.getStatus();

        // pass the control to LeaveapprovalService
        LeaveStatus newStatus = leaveApprovalService.resolveApproval(leave, approver);

        leave.setStatus(newStatus);

        if (newStatus == LeaveStatus.MANAGER_APPROVED) {
            leave.setApprovalStage(ApprovalStage.ADMIN);
        } else {
            leave.setApprovalStage(ApprovalStage.COMPLETED);
        }

        LeaveRequest saved = leaveRequestRepository.save(leave);

        if (newStatus == LeaveStatus.APPROVED) {
            deductBalance(saved);
        }

        writeHistory(saved, oldStatus, newStatus, request.getComment(), approver);

        //  Notify asynchronously after commit
        NotificationDto dto = buildDto(saved);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (newStatus == LeaveStatus.MANAGER_APPROVED) {
                            notificationService.notifyManagerApproved(dto);
                            notificationService.notifyAdminPendingApproval(dto);
                        } else {
                            notificationService.notifyLeaveApproved(dto);
                        }
                    }
                }
        );

        log.info("Leave {} status: {} → {} by:{}", leaveId, oldStatus, newStatus, email);
        return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
    }

    // Reject Leave

    @Transactional
    @Override
    public LeaveStatusResponse rejectLeave(Long leaveId, LeaveActionRequest request, String email) {
        log.info("Reject requested for leaveId:{} by:{}", leaveId, email);
        Employee rejector = getUser(email);

        if (!hasRole(rejector, "ROLE_MANAGER") && !hasRole(rejector, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only Manager or Admin can reject leaves");
        }

        LeaveRequest leave = getLeave(leaveId);

        if (hasRole(rejector, "ROLE_MANAGER") && !hasRole(rejector, "ROLE_ADMIN")) {
            boolean isSubordinate = leave.getEmployee().getManager() != null &&
                    leave.getEmployee().getManager().getId().equals(rejector.getId());
            if (!isSubordinate) {
                throw new AccessDeniedException(
                        "You can only reject leaves of employees directly under you");
            }
        }

        leaveValidationService.validateRejectRequest(leave);

        LeaveStatus oldStatus = leave.getStatus();
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(request.getComment());
        leave.setApprovalStage(ApprovalStage.COMPLETED);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        releaseBalance(saved);
        writeHistory(saved, oldStatus, LeaveStatus.REJECTED, request.getComment(), rejector);

        NotificationDto dto = buildDto(saved);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        notificationService.notifyLeaveRejected(dto);
                    }
                }
        );

        log.info("Leave {} rejected by:{}", leaveId, email);
        return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
    }

    // Cancel Leave

    @Transactional
    @Override
    public LeaveStatusResponse cancelLeave(Long leaveId, LeaveActionRequest request, String email) {
        log.info("Cancel requested for leaveId:{} by:{}", leaveId, email);
        Employee requestor = getUser(email);
        LeaveRequest leave = getLeave(leaveId);

        leaveValidationService.validateCancelRequest(leave, requestor);

        LeaveStatus currentStatus = leave.getStatus();

        NotificationDto dto = buildDto(leave);

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setApprovalStage(ApprovalStage.COMPLETED);
        LeaveRequest saved = leaveRequestRepository.save(leave);

        if (currentStatus == LeaveStatus.APPROVED) {
            leaveBalanceService.restoreUsedUnits(
                    leave.getEmployee().getId(),
                    leave.getLeaveType().getId(),
                    leave.getStartDate().getYear(),
                    leave.getRequestedUnits());
        } else {
            releaseBalance(saved);
        }

        writeHistory(saved, currentStatus, LeaveStatus.CANCELLED, request.getComment(), requestor);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //notificationService.notifyLeaveCancelled(dto);
                        notificationService.notifyLeaveCancelledManager(dto); // own thread
                        notificationService.notifyLeaveCancelledAdmin(dto);   // own thread
                    }
                }
        );

        log.info("Leave {} cancelled by employee:{}", leaveId, email);
        return new LeaveStatusResponse(saved.getId(), saved.getStatus(), saved.getUpdatedAt());
    }
}
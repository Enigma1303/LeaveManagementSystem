package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.LeaveStatusHistory;
import com.aryan.springboot.leavemanagement.entity.enums.SessionType;
import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveStatusRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveHistoryResponse;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.repository.LeaveStatusHistoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveStatusHistoryRepository leaveStatusHistoryRepository;
    private final UserRepository userRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository,
            LeaveStatusHistoryRepository leaveStatusHistoryRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.leaveStatusHistoryRepository = leaveStatusHistoryRepository;
    }

    private Employee getUser(String email) {
        return userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new RuntimeException("User not found: " + email);
                });
    }

    private boolean hasRole(Employee user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getName().equals(role));
    }

    @Override
    public LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email) {
        log.info("Leave submission requested by: {}", email);
        Employee employee = getUser(email);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            log.warn("Invalid dates: endDate {} is before startDate {} for user: {}",
                    request.getEndDate(), request.getStartDate(), email);
            throw new RuntimeException("End date cannot be before start date");
        }
        if (request.getStartDate().isEqual(request.getEndDate())) {
            if (request.getStartSession() == SessionType.SECOND_HALF
                    && request.getEndSession() == SessionType.FIRST_HALF) {
                log.warn("Invalid session combination for same day leave by user: {}", email);
                throw new RuntimeException(
                        "End session cannot be FIRST_HALF when start session is SECOND_HALF on the same day");
            }
        }

        Long overlappingCount = leaveRequestRepository.countOverlappingLeaves(
                employee.getId(), request.getStartDate(), request.getEndDate());
        if (overlappingCount > 0) {
            log.warn("Overlapping leave detected for employee: {} between {} and {}",
                    email, request.getStartDate(), request.getEndDate());
            throw new RuntimeException(
                    "You already have a leave request overlapping with the selected dates");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStartSession(request.getStartSession());
        leave.setEndSession(request.getEndSession());
        LeaveRequest saved = leaveRequestRepository.save(leave);

        log.info("Leave submitted successfully - id: {} by: {}", saved.getId(), email);
        return new LeaveSubmitResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getCreatedAt());
    }

    @Override
    public List<LeaveViewResponse> getLeaves(String email, LeaveStatus status, Long employeeId,
            Long managerId, LocalDate startDate,
            LocalDate endDate, LocalDateTime createdAt, String search) {

        log.info("Fetching leaves for: {} with filters - status: {}, employeeId: {}, managerId: {}",
                email, status, employeeId, managerId);

        Employee user = getUser(email);
        List<LeaveRequest> leaves = new ArrayList<>();

        if (hasRole(user, "ROLE_ADMIN")) {
            leaves = leaveRequestRepository.findAllWithFilters(
                    status, employeeId, managerId, startDate, endDate, createdAt, search);
            log.info("Admin {} fetched {} leave(s)", email, leaves.size());
        } else if (hasRole(user, "ROLE_MANAGER")) {
            leaves.addAll(leaveRequestRepository.findByEmployeeIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search));
            leaves.addAll(leaveRequestRepository.findByManagerIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search));
            leaves.sort(Comparator.comparing(LeaveRequest::getCreatedAt).reversed());
            log.info("Manager {} fetched {} leave(s)", email, leaves.size());
        } else if (hasRole(user, "ROLE_EMPLOYEE")) {
            leaves = leaveRequestRepository.findByEmployeeIdWithFilters(
                    user.getId(), status, startDate, endDate, createdAt, search);
            log.info("Employee {} fetched {} leave(s)", email, leaves.size());
        } else {
            log.warn("Access denied for user: {} - no valid role found", email);
            throw new AccessDeniedException("User does not have a valid role to view leaves");
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
                                h.getCreatedBy().getName(),
                                h.getCreatedAt())).toList())).toList();
    }

    @Transactional
    @Override
    public LeaveStatusResponse updateLeaveStatus(Long leaveId, LeaveStatusRequest request, String email) {
        log.info("Leave status update requested for leaveId: {} by: {}", leaveId, email);

        Employee user = getUser(email);

        if (!hasRole(user, "ROLE_MANAGER") && !hasRole(user, "ROLE_ADMIN")) {
            log.warn("Access denied for user: {} - not a manager or admin", email);
            throw new AccessDeniedException("Only managers and admins can update leave statuses");
        }

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> {
                    log.error("Leave request not found for id: {}", leaveId);
                    return new RuntimeException("No such Leave Request found in the database");
                });

        if (hasRole(user, "ROLE_MANAGER")) {
            boolean isSubordinate = leave.getEmployee().getManager() != null &&
                    leave.getEmployee().getManager().getId().equals(user.getId());
            if (!isSubordinate) {
                log.warn("Manager {} attempted to update leave {} of a non-subordinate employee",
                        email, leaveId);
                throw new AccessDeniedException("You can only update leaves of employees under you");
            }
        }

        LeaveStatus currentStatus = leave.getStatus();
        LeaveStatus newStatus = request.getStatus();

        Map<LeaveStatus, Set<LeaveStatus>> validTransitions = Map.of(
                LeaveStatus.PENDING, Set.of(LeaveStatus.APPROVED, LeaveStatus.REJECTED));

        Set<LeaveStatus> allowed = validTransitions.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            log.warn("Invalid status transition for leaveId: {} from {} to {} by: {}",
                    leaveId, currentStatus, newStatus, email);
            throw new RuntimeException(
                    "Cannot transition leave from " + currentStatus + " to " + newStatus);
        }

        leave.setStatus(request.getStatus());
        leave.setUpdatedAt(LocalDateTime.now());
        LeaveRequest saved = leaveRequestRepository.save(leave);

        LeaveStatusHistory history = new LeaveStatusHistory();
        history.setLeaveRequest(saved);
        history.setOldStatus(currentStatus);
        history.setNewStatus(request.getStatus());
        history.setComment(request.getComment());
        history.setCreatedBy(user);
        history.setCreatedAt(LocalDateTime.now());
        leaveStatusHistoryRepository.save(history);

        log.info("Leave {} status updated from {} to {} by: {}", leaveId, currentStatus, newStatus, email);
        return new LeaveStatusResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getUpdatedAt());
    }
}
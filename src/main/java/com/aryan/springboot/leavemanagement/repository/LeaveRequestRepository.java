package com.aryan.springboot.leavemanagement.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;

public interface LeaveRequestRepository extends
        JpaRepository<LeaveRequest, Long>,
        JpaSpecificationExecutor<LeaveRequest> {

 String COMMON_FILTERS =
         "AND (:status IS NULL OR l.status = :status) " +
                 "AND (:startDate IS NULL OR l.startDate >= :startDate) " +
                 "AND (:endDate IS NULL OR l.endDate <= :endDate) " +
                 "AND (:createdAt IS NULL OR l.createdAt >= :createdAt) " +
                 "AND (:search IS NULL OR LOWER(l.reason) LIKE LOWER(CONCAT('%', :search, '%')) " +
                 "OR LOWER(l.employee.name) LIKE LOWER(CONCAT('%', :search, '%'))) ";

 String BASE_QUERY = "SELECT l FROM LeaveRequest l " +
         "JOIN FETCH l.employee e " +
         "JOIN FETCH l.leaveType lt " +
         "LEFT JOIN FETCH l.statusHistory ";

 String ORDER_BY = "ORDER BY l.createdAt DESC ";

 @Query(BASE_QUERY + "WHERE e.id = :employeeId " + COMMON_FILTERS + ORDER_BY)
 List<LeaveRequest> findByEmployeeIdWithFilters(
         @Param("employeeId") Long employeeId,
         @Param("status") LeaveStatus status,
         @Param("startDate") LocalDate startDate,
         @Param("endDate") LocalDate endDate,
         @Param("createdAt") LocalDateTime createdAt,
         @Param("search") String search);

 @Query(BASE_QUERY + "WHERE e.manager.id = :managerId " + COMMON_FILTERS + ORDER_BY)
 List<LeaveRequest> findByManagerIdWithFilters(
         @Param("managerId") Long managerId,
         @Param("status") LeaveStatus status,
         @Param("startDate") LocalDate startDate,
         @Param("endDate") LocalDate endDate,
         @Param("createdAt") LocalDateTime createdAt,
         @Param("search") String search);

 @Query(BASE_QUERY +
         "WHERE (:employeeId IS NULL OR e.id = :employeeId) " +
         "AND (:managerId IS NULL OR e.manager.id = :managerId) " + COMMON_FILTERS + ORDER_BY)
 List<LeaveRequest> findAllWithFilters(
         @Param("status") LeaveStatus status,
         @Param("employeeId") Long employeeId,
         @Param("managerId") Long managerId,
         @Param("startDate") LocalDate startDate,
         @Param("endDate") LocalDate endDate,
         @Param("createdAt") LocalDateTime createdAt,
         @Param("search") String search);

 @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee.id = :employeeId " +
         "AND l.status NOT IN :excludedStatuses " +
         "AND l.startDate <= :endDate AND l.endDate >= :startDate")
 Long countOverlappingLeaves(
         @Param("employeeId") Long employeeId,
         @Param("startDate") LocalDate startDate,
         @Param("endDate") LocalDate endDate,
         @Param("excludedStatuses") List<LeaveStatus> excludedStatuses);

 @Query("""
SELECT l FROM LeaveRequest l
WHERE l.status = com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus.PENDING
AND l.startDate <= :threshold
""")
 List<LeaveRequest> findPendingLeavesForReminder(@Param("threshold") LocalDate threshold);

 @Query("""
    SELECT DISTINCT l FROM LeaveRequest l
    JOIN FETCH l.employee
    JOIN FETCH l.leaveType
    WHERE l.status IN :statuses
      AND l.startDate BETWEEN :today AND :maxThreshold
""")
 List<LeaveRequest> findPendingLeavesForReminder(
         @Param("statuses") List<LeaveStatus> statuses,
         @Param("today") LocalDate today,
         @Param("maxThreshold") LocalDate maxThreshold);

 @EntityGraph(attributePaths = {
         "employee",
         "leaveType",
         "statusHistory"
 })
 Page<LeaveRequest> findAll(Specification<LeaveRequest> spec, Pageable pageable);

}
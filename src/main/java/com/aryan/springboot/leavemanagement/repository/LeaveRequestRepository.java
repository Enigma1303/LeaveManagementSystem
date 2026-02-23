package com.aryan.springboot.leavemanagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.LeaveStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    String COMMON_FILTERS = 
        "AND (:status IS NULL OR l.status = :status) " +
        "AND (:startDate IS NULL OR l.startDate >= :startDate) " +
        "AND (:endDate IS NULL OR l.endDate <= :endDate) " +
        "AND (:search IS NULL OR LOWER(l.reason) LIKE LOWER(CONCAT('%', :search, '%')) " +
        "OR LOWER(l.employee.name) LIKE LOWER(CONCAT('%', :search, '%')))";
     


    String BASEQUERY = "SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.statusHistory ";

    @Query(BASEQUERY + "WHERE l.employee.id = :employeeId " + COMMON_FILTERS)
    List<LeaveRequest> findByEmployeeIdWithFilters(
        @Param("employeeId") Long employeeId,
        @Param("status") LeaveStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("search") String search);

    @Query(BASEQUERY + "WHERE l.employee.manager.id = :managerId " + COMMON_FILTERS)
    List<LeaveRequest> findByManagerIdWithFilters(
        @Param("managerId") Long managerId,
        @Param("status") LeaveStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("search") String search);

    @Query(BASEQUERY + "WHERE (:employeeId IS NULL OR l.employee.id = :employeeId) " +
           "AND (:managerId IS NULL OR l.employee.manager.id = :managerId) " + COMMON_FILTERS)
    List<LeaveRequest> findAllWithFilters(
        @Param("status") LeaveStatus status,
        @Param("employeeId") Long employeeId,
        @Param("managerId") Long managerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("search") String search);
}
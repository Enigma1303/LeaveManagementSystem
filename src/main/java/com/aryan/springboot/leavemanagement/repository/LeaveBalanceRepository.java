package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    // Used during CSV import to check existing balance
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId,
            Long leaveTypeId,
            Integer year
    );

    // Used when employee requests leave
    @Query("""
            SELECT lb
            FROM LeaveBalance lb
            JOIN FETCH lb.leaveType
            WHERE lb.employee.id = :employeeId
            AND lb.year = :year
           """)
    List<LeaveBalance> findByEmployeeIdAndYear(
            @Param("employeeId") Long employeeId,
            @Param("year") Integer year
    );

    // Used for reporting/export by year
    @Query("""
            SELECT lb
            FROM LeaveBalance lb
            JOIN FETCH lb.leaveType
            JOIN FETCH lb.employee
            WHERE lb.year = :year
           """)
    List<LeaveBalance> findAllByYear(
            @Param("year") Integer year
    );
}
package com.aryan.springboot.leavemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long>{
    //IN JPQL WE SELECT AND WORK WITH ENTITES and NOT COLUMNS

        @Query("Select l FROM LeaveRequest l LEFT JOIN FETCH l.statusHistory WHERE l.employee.id=:employeeId")
        List<LeaveRequest> findByEmployeeId(@Param("employeeId") Long employeeId);
        
        @Query("Select l from LeaveRequest l LEFT JOIN FETCH l.statusHistory WHERE l.employee.manager.id= :managerId")
        List<LeaveRequest>findByManagerId(@Param("managerId") Long managerId);
        
        @Query("Select l FROM LeaveRequest l LEFT JOIN FETCH l.statusHistory")
        List<LeaveRequest>All();
}


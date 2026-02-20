package com.aryan.springboot.leavemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long>{

}

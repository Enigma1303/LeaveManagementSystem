package com.aryan.springboot.leavemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aryan.springboot.leavemanagement.entity.LeaveStatusHistory;

public interface LeaveStatusHistoryRepository extends JpaRepository<LeaveStatusHistory,Long> {

}

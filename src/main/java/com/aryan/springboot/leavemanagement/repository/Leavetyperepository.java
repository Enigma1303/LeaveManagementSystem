package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByName(String name);

    List<LeaveType> findByIsActiveTrue();

    boolean existsByName(String name);
}
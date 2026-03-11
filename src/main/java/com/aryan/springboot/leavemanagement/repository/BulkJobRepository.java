package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.BulkJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkJobRepository extends JpaRepository<BulkJob, Long> {
}
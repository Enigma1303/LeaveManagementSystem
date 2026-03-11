package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.BulkJobError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BulkJobErrorRepository extends JpaRepository<BulkJobError, Long> {

    List<BulkJobError> findByBulkJobId(Long bulkJobId);

}
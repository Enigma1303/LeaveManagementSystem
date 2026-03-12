package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.AuditLog;
import com.aryan.springboot.leavemanagement.entity.enums.AuditAction;
import com.aryan.springboot.leavemanagement.entity.enums.AuditEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AuditEntityType entityType, Long entityId);

    List<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId);
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.AuditLog;
import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.enums.AuditAction;
import com.aryan.springboot.leavemanagement.entity.enums.AuditEntityType;
import com.aryan.springboot.leavemanagement.exception.ResourceNotFoundException;
import com.aryan.springboot.leavemanagement.repository.AuditLogRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository,
                            UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId,
                    AuditEntityType entityType,
                    Long entityId,
                    AuditAction action,
                    String oldValue,
                    String newValue) {

        try {
            Employee actor = userRepository.findById(actorId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Actor not found: " + actorId));

            AuditLog auditLog = new AuditLog();
            auditLog.setActor(actor);

            auditLog.setEntityType(entityType);

            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);

            auditLogRepository.save(auditLog);

            log.info("Audit logged — actor:{} action:{} entity:{} id:{}",
                    actorId, action, entityType, entityId);

        } catch (Exception e) {
            log.error("Audit log failed — actor:{} action:{} entity:{} id:{} error:{}",
                    actorId, action, entityType, entityId, e.getMessage());
        }
    }

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId,
                    AuditEntityType entityType,
                    Long entityId,
                    AuditAction action) {

        log(actorId, entityType, entityId, action, null, null);
    }
}
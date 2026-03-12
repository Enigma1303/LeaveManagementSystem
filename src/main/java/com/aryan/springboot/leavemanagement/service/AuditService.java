package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.enums.AuditAction;
import com.aryan.springboot.leavemanagement.entity.enums.AuditEntityType;

public interface AuditService {

    // Core method
    void log(Long actorId,
             AuditEntityType AuditEntityType,
             Long entityId,
             AuditAction action,
             String oldValue,
             String newValue);

    void log(Long actorId,
             AuditEntityType AuditEntityType,
             Long entityId,
             AuditAction action);
}
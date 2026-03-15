package com.aryan.springboot.leavemanagement.response;

import com.aryan.springboot.leavemanagement.entity.NotificationLog;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationStatus;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationLogResponse {

    private final Long id;
    private final Long leaveRequestId;       // sirf id required taaki lazy load na ho
    private final Long recipientId;
    private final String recipientEmail;
    private final NotificationType type;
    private final NotificationStatus status;
    private final Integer attemptCount;
    private final Integer maxAttempts;
    private final LocalDateTime lastAttemptedAt;
    private final LocalDateTime sentAt;
    private final String errorMessage;
    private final LocalDateTime createdAt;

    public NotificationLogResponse(NotificationLog log) {
        this.id = log.getId();
        this.leaveRequestId = log.getLeaveRequest().getId();
        this.recipientId = log.getRecipient().getId();
        this.recipientEmail = log.getRecipient().getEmail();
        this.type = log.getType();
        this.status = log.getStatus();
        this.attemptCount = log.getAttemptCount();
        this.maxAttempts = log.getMaxAttempts();
        this.lastAttemptedAt = log.getLastAttemptedAt();
        this.sentAt = log.getSentAt();
        this.errorMessage = log.getErrorMessage();
        this.createdAt = log.getCreatedAt();
    }
}
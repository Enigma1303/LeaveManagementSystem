package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.NotificationLog;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationStatus;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import com.aryan.springboot.leavemanagement.exception.ResourceNotFoundException;
import com.aryan.springboot.leavemanagement.repository.NotificationLogRepository;
import com.aryan.springboot.leavemanagement.response.NotificationLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAdminService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<NotificationLogResponse> getFailedNotifications() {
        List<NotificationLog> failed = notificationLogRepository
                .findByStatus(NotificationStatus.FAILED);
        List<NotificationLog> exhausted = notificationLogRepository
                .findByStatus(NotificationStatus.EXHAUSTED);
        failed.addAll(exhausted);
        // map to response DTO taaki voids lazy loading serialization error
        return failed.stream()
                .map(NotificationLogResponse::new)
                .toList();
    }

    @Transactional
    public NotificationLogResponse retrigger(Long notificationId) {
        NotificationLog entry = notificationLogRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found for id: " + notificationId));

        if (entry.getStatus() == NotificationStatus.SENT
                || entry.getStatus() == NotificationStatus.PENDING) {
            throw new BusinessRuleException(
                    "Cannot retrigger notification with status: " + entry.getStatus());
        }

        log.info("Admin retriggering notification id:{} to:{}",
                notificationId, entry.getRecipient().getEmail());

        try {
            emailService.sendEmail(
                    entry.getRecipient().getEmail(),
                    buildSubject(entry),
                    entry.getPayload()
            );

            entry.setStatus(NotificationStatus.SENT);
            entry.setSentAt(LocalDateTime.now());
            entry.setErrorMessage(null);
            log.info("Admin retrigger successful — notification id:{}", notificationId);

        } catch (Exception e) {
            entry.setAttemptCount(entry.getAttemptCount() + 1);
            entry.setLastAttemptedAt(LocalDateTime.now());
            entry.setErrorMessage(e.getMessage());
            entry.setStatus(NotificationStatus.FAILED);
            log.error("Admin retrigger failed — notification id:{} error:{}",
                    notificationId, e.getMessage());
        }

        return new NotificationLogResponse(notificationLogRepository.save(entry));
    }

    private String buildSubject(NotificationLog entry) {
        return switch (entry.getType()) {
            case SUBMISSION       -> "Leave Request Submitted";
            case APPROVED         -> "Leave Request Approved";
            case REJECTED         -> "Leave Request Rejected";
            case CANCELLED        -> "Leave Request Cancelled";
            case MANAGER_APPROVED -> "Leave Request Pending Admin Approval";
            case REMINDER         -> "Reminder: Pending Leave Request";
            default               -> "Leave Notification";
        };
    }
}
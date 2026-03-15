package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.NotificationLog;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationStatus;
import com.aryan.springboot.leavemanagement.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;

    @Value("${app.notification.retry.interval-seconds}")
    private long retryIntervalSeconds;

    @Scheduled(fixedDelayString = "${app.notification.retry.interval-seconds}000")
    @Transactional
    public void retryFailedNotifications() {

        List<NotificationLog> retryable = notificationLogRepository
                .findRetryable(NotificationStatus.FAILED);

        if (retryable.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", retryable.size());

        for (NotificationLog entry : retryable) {

            if (!isReadyForRetry(entry)) {
                log.debug("Notification id:{} not ready for retry yet (backoff)", entry.getId());
                continue;
            }

            try {
                emailService.sendEmail(
                        entry.getRecipient().getEmail(),
                        buildSubject(entry),
                        entry.getPayload()
                );

                entry.setStatus(NotificationStatus.SENT);
                entry.setSentAt(LocalDateTime.now());
                log.info("Retry successful — notification id:{} to:{}",
                        entry.getId(), entry.getRecipient().getEmail());

            } catch (Exception e) {
                entry.setAttemptCount(entry.getAttemptCount() + 1);
                entry.setLastAttemptedAt(LocalDateTime.now());
                entry.setErrorMessage(e.getMessage());

                if (entry.getAttemptCount() >= entry.getMaxAttempts()) {
                    entry.setStatus(NotificationStatus.EXHAUSTED);
                    log.error("Notification id:{} exhausted all {} attempts — marking EXHAUSTED",
                            entry.getId(), entry.getMaxAttempts());
                } else {
                    log.warn("Retry failed for notification id:{} attempt:{}/{}",
                            entry.getId(), entry.getAttemptCount(), entry.getMaxAttempts());
                }
            }

            notificationLogRepository.save(entry);
        }
    }

    private boolean isReadyForRetry(NotificationLog entry) {
        if (entry.getLastAttemptedAt() == null) return true;

        long backoffSeconds = (long) (1 * Math.pow(2, entry.getAttemptCount() - 1));
        LocalDateTime readyAt = entry.getLastAttemptedAt().plusSeconds(backoffSeconds);
        return LocalDateTime.now().isAfter(readyAt);
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
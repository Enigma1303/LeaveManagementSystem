package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.NotificationLog;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationStatus;
import com.aryan.springboot.leavemanagement.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;

    private static final int MAX_ATTEMPTS = 3;

    @Scheduled(fixedDelay = 300000) // 5 minutes ke baad retry karo
    public void retryFailedNotifications() {

        List<NotificationLog> failedNotifications =
                notificationLogRepository.findByStatusAndAttemptCountLessThan(
                        NotificationStatus.FAILED,
                        MAX_ATTEMPTS
                );

        for (NotificationLog logEntry : failedNotifications) {

            try {

                emailService.sendEmail(
                        logEntry.getRecipient().getEmail(),
                        "Retry Notification",
                        logEntry.getPayload()
                );

                logEntry.setStatus(NotificationStatus.SENT);
                logEntry.setSentAt(LocalDateTime.now());

                log.info("Notification retry successful id: {}", logEntry.getId());

            } catch (Exception e) {

                logEntry.setAttemptCount(logEntry.getAttemptCount() + 1);
                logEntry.setLastAttemptedAt(LocalDateTime.now());
                logEntry.setErrorMessage(e.getMessage());

                log.warn("Retry failed for notification id: {}", logEntry.getId());
            }

            notificationLogRepository.save(logEntry);
        }
    }
}
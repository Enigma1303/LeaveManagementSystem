package com.aryan.springboot.leavemanagement.scheduler;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.service.NotificationService;
import com.aryan.springboot.leavemanagement.service.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveReminderScheduler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;

    @Value("${leave.reminder.days-before-start}")
    private int reminderDays;

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendPendingLeaveReminders() {

        LocalDate threshold = LocalDate.now().plusDays(reminderDays);

        List<LeaveRequest> pendingLeaves =
                leaveRequestRepository.findPendingLeavesForReminder(threshold);

        for (LeaveRequest leave : pendingLeaves) {

            NotificationDto dto = NotificationDto.from(leave);

            notificationService.notifyLeaveSubmitted(dto);

            log.info("Reminder sent for pending leave id: {}", leave.getId());
        }
    }
}
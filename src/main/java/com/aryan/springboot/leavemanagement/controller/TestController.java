package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.service.LeaveReminderScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final LeaveReminderScheduler leaveReminderScheduler;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-reminder")
    public String testReminder() {
        leaveReminderScheduler.sendPendingLeaveReminders();
        return "Reminder triggered";
    }
}
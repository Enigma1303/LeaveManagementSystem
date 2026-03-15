package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserRepository userRepository;

    @Scheduled(cron = "${app.leave.reminder.cron:0 0 9 * * *}")
    public void sendPendingLeaveReminders() {
        LocalDate today = LocalDate.now();

        LocalDate maxThreshold = today.plusDays(30);

        List<LeaveRequest> candidates =
                leaveRequestRepository.findPendingLeavesForReminder(
                        List.of(LeaveStatus.PENDING, LeaveStatus.MANAGER_APPROVED),
                        today,
                        maxThreshold
                );

        if (candidates.isEmpty()) {
            log.info("No pending leaves found for reminder today");
            return;
        }

        log.info("Found {} candidate leave(s) for reminder check", candidates.size());

        Employee admin = userRepository.findByRole("ROLE_ADMIN").orElse(null);

        for (LeaveRequest leave : candidates) {

            int thresholdDays = leave.getLeaveType().getReminderThresholdDays();
            LocalDate reminderDeadline = today.plusDays(thresholdDays);

            if (leave.getStartDate().isAfter(reminderDeadline)) {
                continue;
            }

            log.info("Sending reminder for leaveId:{} startDate:{} status:{} threshold:{}days",
                    leave.getId(), leave.getStartDate(), leave.getStatus(), thresholdDays);

            Employee employee = leave.getEmployee();
            Employee manager = employee.getManager();

            if (leave.getStatus() == LeaveStatus.MANAGER_APPROVED) {
                sendAdminReminder(leave, admin);

            } else if (leave.getStatus() == LeaveStatus.PENDING) {

                if (manager == null) {
                    log.info("No manager for employee:{} — sending reminder to admin for leaveId:{}",
                            employee.getEmail(), leave.getId());
                    sendAdminReminder(leave, admin);
                } else {
                    sendManagerReminder(leave, manager);
                }
            }
        }
    }

    private void sendManagerReminder(LeaveRequest leave, Employee manager) {
        String subject = "Reminder: Pending Leave Request from "
                + leave.getEmployee().getName();
        String body = "Dear " + manager.getName() + ",\n\n"
                + "This is a reminder that the following leave request is still pending your approval.\n\n"
                + "Employee    : " + leave.getEmployee().getName() + "\n"
                + "Leave Type  : " + leave.getLeaveType().getName() + "\n"
                + "Start Date  : " + leave.getStartDate() + "\n"
                + "End Date    : " + leave.getEndDate() + "\n"
                + "Duration    : " + leave.getRequestedUnits() + " day(s)\n"
                + "Reason      : " + leave.getReason() + "\n\n"
                + "Please login to approve or reject this request before the leave starts.\n\n"
                + "Leave Management System";

        notificationService.sendReminderEmail(manager.getEmail(), subject, body, leave.getId(),
                manager.getId());
        log.info("Manager reminder sent for leaveId:{} to:{}", leave.getId(), manager.getEmail());
    }

    private void sendAdminReminder(LeaveRequest leave, Employee admin) {
        if (admin == null) {
            log.warn("No admin found — cannot send reminder for leaveId:{}", leave.getId());
            return;
        }
        String subject = "Reminder: Pending Leave Request from "
                + leave.getEmployee().getName();
        String body = "Dear " + admin.getName() + ",\n\n"
                + "This is a reminder that the following leave request requires your action.\n\n"
                + "Employee    : " + leave.getEmployee().getName() + "\n"
                + "Leave Type  : " + leave.getLeaveType().getName() + "\n"
                + "Start Date  : " + leave.getStartDate() + "\n"
                + "End Date    : " + leave.getEndDate() + "\n"
                + "Duration    : " + leave.getRequestedUnits() + " day(s)\n"
                + "Status      : " + leave.getStatus() + "\n"
                + "Reason      : " + leave.getReason() + "\n\n"
                + "Please login to approve or reject this request before the leave starts.\n\n"
                + "Leave Management System";

        notificationService.sendReminderEmail(admin.getEmail(), subject, body, leave.getId(),
                admin.getId());
        log.info("Admin reminder sent for leaveId:{} to:{}", leave.getId(), admin.getEmail());
    }
}
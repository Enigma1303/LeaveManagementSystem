package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.service.NotificationDto;
import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.NotificationLog;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationStatus;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationType;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.NotificationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final NotificationLogRepository notificationLogRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public NotificationServiceImpl(EmailService emailService,
                                   NotificationLogRepository notificationLogRepository,
                                   LeaveRequestRepository leaveRequestRepository) {
        this.emailService = emailService;
        this.notificationLogRepository = notificationLogRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    // Leave Submitted → notify Manager

    @Async("notificationExecutor")
    @Override
    public void notifyLeaveSubmitted(NotificationDto dto) {
        if (dto.getManagerEmail() == null) {
            log.warn("No manager assigned for leaveId:{} skipping submission notification",
                    dto.getLeaveId());
            return;
        }
        String subject = "New Leave Request from " + dto.getEmployeeName();
        String body = "Dear " + dto.getManagerName() + ",\n\n"
                + dto.getEmployeeName() + " has submitted a leave request.\n\n"
                + details(dto)
                + "Approval Stage : " + dto.getApprovalStage().name() + "\n"
                + balanceLine(dto)
                + "\nPlease login to approve or reject this request."
                + footer();
        sendAndLog(dto.getLeaveId(), dto.getManagerEmail(),
                NotificationType.SUBMISSION, subject, body);
    }

    // Leave Approved → notify Employee

    @Async("notificationExecutor")
    @Override
    public void notifyLeaveApproved(NotificationDto dto) {
        String subject = "Your Leave Request has been Approved";
        String body = "Dear " + dto.getEmployeeName() + ",\n\n"
                + "Your leave request has been approved.\n\n"
                + details(dto)
                + balanceLine(dto)
                + footer();
        sendAndLog(dto.getLeaveId(), dto.getEmployeeEmail(),
                NotificationType.APPROVED, subject, body);
    }

    // Leave Rejected → notify Employee

    @Async("notificationExecutor")
    @Override
    public void notifyLeaveRejected(NotificationDto dto) {
        String subject = "Your Leave Request has been Rejected";
        String body = "Dear " + dto.getEmployeeName() + ",\n\n"
                + "Your leave request has been rejected.\n\n"
                + details(dto)
                + (dto.getRejectionReason() != null
                ? "Rejection Note : " + dto.getRejectionReason() + "\n" : "")
                + footer();
        sendAndLog(dto.getLeaveId(), dto.getEmployeeEmail(),
                NotificationType.REJECTED, subject, body);
    }

    // Leave Cancelled → notify Manager + Admin if admin was involved

    @Async("notificationExecutor")
    @Override
    public void notifyLeaveCancelled(NotificationDto dto) {
        String subject = dto.getEmployeeName() + " has cancelled their leave";
        String body = "has cancelled their leave request.";

        // Always notify manager
        if (dto.getManagerEmail() != null) {
            String managerBody = "Dear " + dto.getManagerName() + ",\n\n"
                    + dto.getEmployeeName() + " " + body + "\n\n"
                    + details(dto)
                    + footer();
            sendAndLog(dto.getLeaveId(), dto.getManagerEmail(),
                    NotificationType.CANCELLED, subject, managerBody);
        } else {
            log.warn("No manager assigned for leaveId:{} skipping manager cancellation notification",
                    dto.getLeaveId());
        }

        // Notify admin if they were involved (status was MANAGER_APPROVED or APPROVED)
        boolean adminWasInvolved = dto.getStatus() == LeaveStatus.MANAGER_APPROVED
                || dto.getStatus() == LeaveStatus.APPROVED;

        if (adminWasInvolved && dto.getAdminEmail() != null) {
            String adminBody = "Dear " + dto.getAdminName() + ",\n\n"
                    + dto.getEmployeeName() + " " + body + "\n\n"
                    + details(dto)
                    + "Note : This leave had reached admin approval stage.\n"
                    + footer();
            sendAndLog(dto.getLeaveId(), dto.getAdminEmail(),
                    NotificationType.CANCELLED, subject, adminBody);
            log.info("Admin notified of cancellation for leaveId:{}", dto.getLeaveId());
        }
    }


    // Manager Approved → notify Employee (multi-level)

    @Async("notificationExecutor")
    @Override
    public void notifyManagerApproved(NotificationDto dto) {
        String subject = "Your Leave Request is Pending Admin Approval";
        String body = "Dear " + dto.getEmployeeName() + ",\n\n"
                + "Your leave request has been approved by your manager "
                + "and is now awaiting final approval from Admin.\n\n"
                + details(dto)
                + "Approval Stage : " + dto.getApprovalStage().name() + "\n"
                + balanceLine(dto)
                + footer();
        sendAndLog(dto.getLeaveId(), dto.getEmployeeEmail(),
                NotificationType.APPROVED, subject, body);
    }

    // Admin notified when multi-level leave needs final approval

    @Async("notificationExecutor")
    @Override
    public void notifyAdminPendingApproval(NotificationDto dto) {
        if (dto.getAdminEmail() == null) {
            log.warn("No admin email found for leaveId:{} skipping admin notification",
                    dto.getLeaveId());
            return;
        }
        String subject = "Leave Request Pending Your Approval - " + dto.getEmployeeName();
        String body = "Dear " + dto.getAdminName() + ",\n\n"
                + "A leave request from " + dto.getEmployeeName()
                + " has been approved by their manager and requires your final approval.\n\n"
                + details(dto)
                + "Approval Stage : " + dto.getApprovalStage().name() + "\n"
                + balanceLine(dto)
                + "\nPlease login to approve or reject this request."
                + footer();
        sendAndLog(dto.getLeaveId(), dto.getAdminEmail(),
                NotificationType.SUBMISSION, subject, body);
    }

    //send email and persist log

    private void sendAndLog(Long leaveId, String recipientEmail,
                            NotificationType type, String subject, String body) {

        LeaveRequest leaveRef = leaveRequestRepository.getReferenceById(leaveId);

        NotificationLog entry = new NotificationLog();
        entry.setLeaveRequest(leaveRef);
        entry.setType(type);
        entry.setStatus(NotificationStatus.PENDING);
        entry.setPayload(body);
        entry.setAttemptCount(1);
        entry.setLastAttemptedAt(LocalDateTime.now());
        NotificationLog saved = notificationLogRepository.save(entry);

        try {
            emailService.sendEmail(recipientEmail, subject, body);
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
            log.info("Notification SENT type:{} to:{}", type, recipientEmail);
        } catch (Exception e) {
            saved.setStatus(NotificationStatus.FAILED);
            saved.setErrorMessage(e.getMessage());
            log.error("Notification FAILED type:{} to:{} error:{}",
                    type, recipientEmail, e.getMessage());
        } finally {
            notificationLogRepository.save(saved);
        }
    }

    private String details(NotificationDto dto) {
        return "Details:\n"
                + "Leave Type : " + dto.getLeaveTypeName() + "\n"
                + "From       : " + dto.getStartDate().format(DATE_FORMAT) + "\n"
                + "To         : " + dto.getEndDate().format(DATE_FORMAT) + "\n"
                + "Duration   : " + dto.getRequestedUnits() + " day(s)\n"
                + (dto.getReason() != null
                ? "Reason     : " + dto.getReason() + "\n" : "");
    }

    private String balanceLine(NotificationDto dto) {
        return dto.getRemainingBalance() != null
                ? "Remaining Balance : " + dto.getRemainingBalance() + " day(s)\n"
                : "";
    }

    private String footer() {
        return "\n\nLeave Management System";
    }
}
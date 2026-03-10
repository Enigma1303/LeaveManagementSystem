package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NotificationDto {

    // Leave info
    private final Long leaveId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer requestedUnits;
    private final String reason;
    private final String rejectionReason;
    private final LeaveStatus status;
    private final ApprovalStage approvalStage;

    private final String leaveTypeName;

    private final Long employeeId;
    private final String employeeName;
    private final String employeeEmail;
    private final String managerName;
    private final String managerEmail;

    private final String adminName;
    private final String adminEmail;

    private final Integer remainingBalance;
}
package com.aryan.springboot.leavemanagement.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricsResponse {

    private long totalLeaveRequests;
    private long totalApprovedLeaves;
    private long totalRejectedLeaves;
    private double avgApprovalTimeHours;
    private long totalHolidayApiCalls;
    private long totalNotificationsSent;
}
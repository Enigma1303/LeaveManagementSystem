package com.aryan.springboot.leavemanagement.projection;

public interface MetricsProjection {

    Long getTotalLeaveRequests();

    Long getTotalApprovedLeaves();

    Long getTotalRejectedLeaves();

    Double getAvgApprovalTimeHours();

    Long getTotalHolidayApiCalls();

    Long getTotalNotificationsSent();
}
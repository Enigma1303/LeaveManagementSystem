package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.response.MetricsResponse;
import com.aryan.springboot.leavemanagement.projection.MetricsProjection;
import com.aryan.springboot.leavemanagement.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MetricsRepository metricsRepository;

    public MetricsResponse getMetrics(int hours) {

        LocalDateTime start = LocalDateTime.now().minusHours(hours);

        MetricsProjection metrics = metricsRepository.getMetrics(start);

        return new MetricsResponse(
                metrics.getTotalLeaveRequests(),
                metrics.getTotalApprovedLeaves(),
                metrics.getTotalRejectedLeaves(),
                metrics.getAvgApprovalTimeHours() == null ? 0.0 : metrics.getAvgApprovalTimeHours(),
                metrics.getTotalHolidayApiCalls(),
                metrics.getTotalNotificationsSent()
        );
    }
}
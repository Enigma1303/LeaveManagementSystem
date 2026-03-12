package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.response.MetricsResponse;
import com.aryan.springboot.leavemanagement.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public MetricsResponse getMetrics(@RequestParam int hours) {
        return metricsService.getMetrics(hours);
    }
}
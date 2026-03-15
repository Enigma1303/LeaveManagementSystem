package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.response.NotificationLogResponse;
import com.aryan.springboot.leavemanagement.service.NotificationAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationAdminService notificationAdminService;

    @GetMapping("/failed")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<NotificationLogResponse>> getFailedNotifications() {
        log.info("GET /api/v2/notifications/failed");
        return ResponseEntity.ok(notificationAdminService.getFailedNotifications());
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NotificationLogResponse> retriggerNotification(@PathVariable Long id) {
        log.info("POST /api/v2/notifications/{}/retry", id);
        return ResponseEntity.ok(notificationAdminService.retrigger(id));
    }
}
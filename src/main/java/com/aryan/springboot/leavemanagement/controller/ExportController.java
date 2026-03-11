package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.service.LeaveRequestExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/leave-requests")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ExportController {

    private final LeaveRequestExportService leaveRequestExportService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLeaveRequests() {

        String csv = leaveRequestExportService.exportLeaveRequestsCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=leave_requests.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.getBytes());
    }
}
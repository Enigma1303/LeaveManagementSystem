package com.aryan.springboot.leavemanagement.controller;
import com.aryan.springboot.leavemanagement.service.LeaveBalanceExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.aryan.springboot.leavemanagement.entity.BulkJob;
import com.aryan.springboot.leavemanagement.repository.BulkJobRepository;
import com.aryan.springboot.leavemanagement.response.BulkJobResponse;
import com.aryan.springboot.leavemanagement.service.LeaveBalanceImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/leave-balances")
@PreAuthorize("hasRole('ADMIN')")
public class LeaveBalanceBulkController {

    private final LeaveBalanceImportService importService;
    private final BulkJobRepository bulkJobRepository;
    private final LeaveBalanceExportService exportService;
    public LeaveBalanceBulkController(
            LeaveBalanceImportService importService,
            LeaveBalanceExportService exportService,
            BulkJobRepository bulkJobRepository) {

        this.importService = importService;
        this.exportService = exportService;
        this.bulkJobRepository = bulkJobRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLeaveBalances() {

        String csv = exportService.exportLeaveBalancesToCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave_balances.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.getBytes());
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importLeaveBalances(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long jobId = importService.startImport(file, userDetails.getUsername());

        return ResponseEntity.accepted().body(
                Map.of(
                        "jobId", jobId,
                        "status", "QUEUED"
                )
        );
    }

    @GetMapping("/import/{jobId}")
    public ResponseEntity<BulkJobResponse> getImportStatus(@PathVariable Long jobId) {

        BulkJob job = bulkJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        BulkJobResponse response = new BulkJobResponse(
                job.getId(),
                job.getStatus(),
                job.getTotalRecords(),
                job.getSuccessfulRecords(),
                job.getFailedRecords(),
                job.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
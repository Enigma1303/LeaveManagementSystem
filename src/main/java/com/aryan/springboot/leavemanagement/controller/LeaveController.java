package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.request.LeaveActionRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;
import com.aryan.springboot.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v2/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Submit Leave
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveSubmitResponse> submitLeave(
            @Valid @RequestBody LeaveSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.submitLeave(request, userDetails.getUsername()));
    }

    // Get Leaves
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LeaveViewResponse>> getLeaves(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        return ResponseEntity.ok(
                leaveService.getLeaves(
                        userDetails.getUsername(),
                        status,
                        employeeId,
                        managerId,
                        startDate,
                        endDate,
                        createdAt,
                        search,
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    // Approve Leave
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveStatusResponse> approveLeave(
            @PathVariable Long id,
            @RequestBody LeaveActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(leaveService.approveLeave(id, request, userDetails.getUsername()));
    }

    // Reject Leave
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveStatusResponse> rejectLeave(
            @PathVariable Long id,
            @RequestBody LeaveActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(leaveService.rejectLeave(id, request, userDetails.getUsername()));
    }

    // Cancel Leave
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveStatusResponse> cancelLeave(
            @PathVariable Long id,
            @RequestBody LeaveActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(leaveService.cancelLeave(id, request, userDetails.getUsername()));
    }
}
package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.response.LeaveBalanceResponse;
import com.aryan.springboot.leavemanagement.service.LeaveBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v2/leave-balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveBalanceResponse>> getMyBalances(
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/v2/leave-balances - user: {}", userDetails.getUsername());
        return ResponseEntity.ok(leaveBalanceService.getMyBalances(userDetails.getUsername(), year));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LeaveBalanceResponse>> getEmployeeBalances(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year) {
        log.info("GET /api/v2/leave-balances/{} - year: {}", employeeId, year);
        return ResponseEntity.ok(leaveBalanceService.getBalancesByEmployeeId(employeeId, year));
    }
}
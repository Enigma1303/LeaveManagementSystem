package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.request.LeaveTypeRequest;
import com.aryan.springboot.leavemanagement.response.LeaveTypeResponse;
import com.aryan.springboot.leavemanagement.service.LeaveTypeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponse> create(@Valid @RequestBody LeaveTypeRequest request) {
        log.info("POST /api/leave-types");
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveTypeService.createLeaveType(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody LeaveTypeRequest request) {
        log.info("PUT /api/leave-types/{}", id);
        return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponse> deactivate(@PathVariable Long id) {
        log.info("DELETE /api/leave-types/{}", id);
        return ResponseEntity.ok(leaveTypeService.deactivateLeaveType(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveTypeResponse>> getAll() {
        log.info("GET /api/leave-types");
        return ResponseEntity.ok(leaveTypeService.getAllLeaveTypes());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LeaveTypeResponse>> getActive() {
        log.info("GET /api/leave-types/active");
        return ResponseEntity.ok(leaveTypeService.getActiveLeaveTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeResponse> getById(@PathVariable Long id) {
        log.info("GET /api/leave-types/{}", id);
        return ResponseEntity.ok(leaveTypeService.getLeaveTypeById(id));
    }
}
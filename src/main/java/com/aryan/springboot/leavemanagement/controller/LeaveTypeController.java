package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.request.LeaveTypeRequest;
import com.aryan.springboot.leavemanagement.response.LeaveTypeResponse;
import com.aryan.springboot.leavemanagement.service.LeaveTypeService;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;
    private final UserRepository userRepository; 

    public LeaveTypeController(LeaveTypeService leaveTypeService,
                               UserRepository userRepository) { 
        this.leaveTypeService = leaveTypeService;
        this.userRepository = userRepository; 
    }

    private Long getActorId(UserDetails userDetails) { 
        return userRepository.findByEmailWithAuthorities(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + userDetails.getUsername()))
                .getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponse> create(
            @Valid @RequestBody LeaveTypeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) { 
        log.info("POST /api/leave-types");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveTypeService.createLeaveType(request, getActorId(userDetails))); 
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LeaveTypeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) { 
        log.info("PUT /api/leave-types/{}", id);
        return ResponseEntity.ok(
                leaveTypeService.updateLeaveType(id, request, getActorId(userDetails))); 
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponse> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) { 
        log.info("DELETE /api/leave-types/{}", id);
        return ResponseEntity.ok(
                leaveTypeService.deactivateLeaveType(id, getActorId(userDetails))); 
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
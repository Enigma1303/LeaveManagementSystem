package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.entity.Users;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveStatusRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;
import com.aryan.springboot.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;

    public LeaveController(LeaveService leaveService, UserRepository userRepository) {
        this.leaveService = leaveService;
        this.userRepository=userRepository;
    }

    @PostMapping
    public ResponseEntity<LeaveSubmitResponse> submitLeave(
            @Valid @RequestBody LeaveSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveService.submitLeave(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<LeaveViewResponse>> getLeaves(
            @AuthenticationPrincipal UserDetails userDetails) {
        Users user = userRepository.findByEmailWithAuthorities(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(leaveService.getLeaves(user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LeaveStatusResponse> updateLeaveStatus(@PathVariable Long id,
                                                                 @Valid @RequestBody LeaveStatusRequest request, 
                                                                @AuthenticationPrincipal UserDetails userDetails)
    {
        Users user=userRepository.findByEmailWithAuthorities(userDetails.getUsername())
                .orElseThrow(()->new RuntimeException("User not found"));
        
        return ResponseEntity.ok(leaveService.updateLeaveStatus(id, request, user));        
        
    }                                                         

                                                
}
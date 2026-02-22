package com.aryan.springboot.leavemanagement.response;

import com.aryan.springboot.leavemanagement.entity.LeaveStatus;
import java.time.LocalDateTime;

public class LeaveSubmitResponse {

    private Long id;
    private LeaveStatus status;
    private LocalDateTime createdAt;

    public LeaveSubmitResponse(Long id, LeaveStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public LeaveStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
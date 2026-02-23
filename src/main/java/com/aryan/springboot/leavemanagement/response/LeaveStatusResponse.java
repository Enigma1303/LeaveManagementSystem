package com.aryan.springboot.leavemanagement.response;

import com.aryan.springboot.leavemanagement.entity.LeaveStatus;
import java.time.LocalDateTime;

public class LeaveStatusResponse {

    private Long id;
    private LeaveStatus status;
    private LocalDateTime updatedAt;

    public LeaveStatusResponse(Long id, LeaveStatus status, LocalDateTime updatedAt) {
        this.id = id;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    
}
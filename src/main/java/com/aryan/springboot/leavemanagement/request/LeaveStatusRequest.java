package com.aryan.springboot.leavemanagement.request;

import com.aryan.springboot.leavemanagement.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public class LeaveStatusRequest {

    @NotNull(message = "Status is required")
    private LeaveStatus status;

    private String comment;

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    
}
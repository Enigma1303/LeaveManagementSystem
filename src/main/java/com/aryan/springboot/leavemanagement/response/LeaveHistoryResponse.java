package com.aryan.springboot.leavemanagement.response;

import com.aryan.springboot.leavemanagement.entity.LeaveStatus;
import java.time.LocalDateTime;

public class LeaveHistoryResponse {

    private LeaveStatus oldStatus;
    private LeaveStatus newStatus;
    private String comment;
    private String changedBy;
    private LocalDateTime createdAt;

    public LeaveHistoryResponse(LeaveStatus oldStatus, LeaveStatus newStatus,
                                 String comment, String changedBy, LocalDateTime createdAt) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.comment = comment;
        this.changedBy = changedBy;
        this.createdAt = createdAt;
    }

    public LeaveStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(LeaveStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public LeaveStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(LeaveStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

  
}
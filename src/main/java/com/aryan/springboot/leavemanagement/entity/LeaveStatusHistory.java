package com.aryan.springboot.leavemanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_status_history")
public class LeaveStatusHistory {

    public LeaveStatusHistory(){}

    public LeaveStatusHistory(@NotNull LeaveRequest leaveRequest, LeaveStatus oldStatus, @NotNull LeaveStatus newStatus,
            @NotNull Users createdBy, String comment) {
        this.leaveRequest = leaveRequest;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.createdBy = createdBy;
        this.comment = comment;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "leave_id", nullable = false)
    private LeaveRequest leaveRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private LeaveStatus oldStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private LeaveStatus newStatus;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Users createdBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LeaveRequest getLeaveRequest() {
        return leaveRequest;
    }

    public void setLeaveRequest(LeaveRequest leaveRequest) {
        this.leaveRequest = leaveRequest;
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

    public Users getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Users createdBy) {
        this.createdBy = createdBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    

}
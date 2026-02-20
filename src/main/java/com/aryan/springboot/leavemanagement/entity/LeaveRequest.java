package com.aryan.springboot.leavemanagement.entity;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    
    public LeaveRequest(){}
    public LeaveRequest(@NotNull Users employee, @NotNull LocalDate startDate, @NotNull LocalDate endDate,
            @NotNull String reason, SessionType startSession, SessionType endSession, LeaveStatus status) {
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.startSession = startSession;
        this.endSession = endSession;
        this.status = LeaveStatus.PENDING;
    }

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Users employee;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_session")
    private SessionType startSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_session")
    private SessionType endSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "leaveRequest")
    private Set<LeaveStatusHistory> statusHistory = new HashSet<>();

    @PrePersist
protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.status = LeaveStatus.PENDING;
}

@PreUpdate
protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getEmployee() {
        return employee;
    }

    public void setEmployee(Users employee) {
        this.employee = employee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public SessionType getStartSession() {
        return startSession;
    }

    public void setStartSession(SessionType startSession) {
        this.startSession = startSession;
    }

    public SessionType getEndSession() {
        return endSession;
    }

    public void setEndSession(SessionType endSession) {
        this.endSession = endSession;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<LeaveStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(Set<LeaveStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    
}


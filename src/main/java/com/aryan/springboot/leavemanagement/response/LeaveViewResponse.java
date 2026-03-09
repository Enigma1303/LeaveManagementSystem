package com.aryan.springboot.leavemanagement.response;

import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.Session;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class LeaveViewResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Session startSession;
    private Session endSession;
    private String reason;
    private LeaveStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    private List<LeaveHistoryResponse> history;

    public LeaveViewResponse(Long id, Long employeeId, String employeeName, LocalDate startDate,
                         LocalDate endDate, Session startSession, Session endSession,
                         String reason, LeaveStatus status, LocalDateTime createdAt,
                         List<LeaveHistoryResponse> history) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startSession = startSession;
        this.endSession = endSession;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.history = history;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public Session getStartSession() {
        return startSession;
    }

    public void setStartSession(Session startSession) {
        this.startSession = startSession;
    }

    public Session getEndSession() {
        return endSession;
    }

    public void setEndSession(Session endSession) {
        this.endSession = endSession;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public List<LeaveHistoryResponse> getHistory() {
        return history;
    }

    public void setHistory(List<LeaveHistoryResponse> history) {
        this.history = history;
    }

   
}
package com.aryan.springboot.leavemanagement.request;

import com.aryan.springboot.leavemanagement.entity.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LeaveSubmitRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Start session is required")
    private SessionType startSession;

    @NotNull(message = "End session is required")
    private SessionType endSession;

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

    
}
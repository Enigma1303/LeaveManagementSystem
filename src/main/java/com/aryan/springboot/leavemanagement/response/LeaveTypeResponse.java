package com.aryan.springboot.leavemanagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class LeaveTypeResponse {

    private Long id;
    private String name;
    private Integer maxUnitsPerRequest;
    private Integer minAdvanceNoticeDays;
    private Boolean isMultiLevelApproval;
    private Integer multiLevelTriggerUnits;
    private Integer reminderThresholdDays;
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public LeaveTypeResponse(Long id, String name, Integer maxUnitsPerRequest,
                             Integer minAdvanceNoticeDays, Boolean isMultiLevelApproval,
                             Integer multiLevelTriggerUnits, Integer reminderThresholdDays,
                             Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.maxUnitsPerRequest = maxUnitsPerRequest;
        this.minAdvanceNoticeDays = minAdvanceNoticeDays;
        this.isMultiLevelApproval = isMultiLevelApproval;
        this.multiLevelTriggerUnits = multiLevelTriggerUnits;
        this.reminderThresholdDays = reminderThresholdDays;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getMaxUnitsPerRequest() { return maxUnitsPerRequest; }
    public Integer getMinAdvanceNoticeDays() { return minAdvanceNoticeDays; }
    public Boolean getIsMultiLevelApproval() { return isMultiLevelApproval; }
    public Integer getMultiLevelTriggerUnits() { return multiLevelTriggerUnits; }
    public Integer getReminderThresholdDays() { return reminderThresholdDays; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
package com.aryan.springboot.leavemanagement.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeaveTypeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private Boolean isActive;
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @NotNull(message = "Max units per request is required")
    @Min(value = 1, message = "Max units must be at least 1")
    private Integer maxUnitsPerRequest;

    @NotNull(message = "Min advance notice days is required")
    @Min(value = 0, message = "Min advance notice days cannot be negative")
    private Integer minAdvanceNoticeDays;

    @NotNull(message = "Multi-level approval flag is required")
    private Boolean isMultiLevelApproval;

    // jab ye ho tab-> isMultiLevelApproval = true
    private Integer multiLevelTriggerUnits;

    @NotNull(message = "Reminder threshold days is required")
    @Min(value = 1, message = "Reminder threshold must be at least 1 day")
    private Integer reminderThresholdDays;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMaxUnitsPerRequest() { return maxUnitsPerRequest; }
    public void setMaxUnitsPerRequest(Integer maxUnitsPerRequest) { this.maxUnitsPerRequest = maxUnitsPerRequest; }

    public Integer getMinAdvanceNoticeDays() { return minAdvanceNoticeDays; }
    public void setMinAdvanceNoticeDays(Integer minAdvanceNoticeDays) { this.minAdvanceNoticeDays = minAdvanceNoticeDays; }

    public Boolean getIsMultiLevelApproval() { return isMultiLevelApproval; }
    public void setIsMultiLevelApproval(Boolean isMultiLevelApproval) { this.isMultiLevelApproval = isMultiLevelApproval; }

    public Integer getMultiLevelTriggerUnits() { return multiLevelTriggerUnits; }
    public void setMultiLevelTriggerUnits(Integer multiLevelTriggerUnits) { this.multiLevelTriggerUnits = multiLevelTriggerUnits; }

    public Integer getReminderThresholdDays() { return reminderThresholdDays; }
    public void setReminderThresholdDays(Integer reminderThresholdDays) { this.reminderThresholdDays = reminderThresholdDays; }
}
package com.aryan.springboot.leavemanagement.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class LeaveBalanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private Integer year;
    private Integer allocatedUnits;
    private Integer usedUnits;
    private Integer pendingUnits;
    private Integer availableUnits;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public LeaveBalanceResponse(Long id, Long employeeId, String employeeName,
                                Long leaveTypeId, String leaveTypeName, Integer year,
                                Integer allocatedUnits, Integer usedUnits, Integer pendingUnits,
                                Integer availableUnits, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveTypeId = leaveTypeId;
        this.leaveTypeName = leaveTypeName;
        this.year = year;
        this.allocatedUnits = allocatedUnits;
        this.usedUnits = usedUnits;
        this.pendingUnits = pendingUnits;
        this.availableUnits = availableUnits;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public Long getLeaveTypeId() { return leaveTypeId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public Integer getYear() { return year; }
    public Integer getAllocatedUnits() { return allocatedUnits; }
    public Integer getUsedUnits() { return usedUnits; }
    public Integer getPendingUnits() { return pendingUnits; }
    public Integer getAvailableUnits() { return availableUnits; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
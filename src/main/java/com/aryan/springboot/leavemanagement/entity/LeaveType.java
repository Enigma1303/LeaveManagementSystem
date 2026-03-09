package com.aryan.springboot.leavemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "max_units_per_request", nullable = false)
    private Integer maxUnitsPerRequest;

    @Column(name = "min_advance_notice_days", nullable = false)
    private Integer minAdvanceNoticeDays;

    @Column(name = "is_multi_level_approval", nullable = false)
    private Boolean isMultiLevelApproval = false;

    @Column(name = "multi_level_trigger_units")
    private Integer multiLevelTriggerUnits;

    @Column(name = "reminder_threshold_days", nullable = false)
    private Integer reminderThresholdDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
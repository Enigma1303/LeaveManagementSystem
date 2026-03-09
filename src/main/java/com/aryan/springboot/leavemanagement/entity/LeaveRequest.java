package com.aryan.springboot.leavemanagement.entity;

import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.entity.enums.Session;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_session", nullable = false)
    private Session startSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_session", nullable = false)
    private Session endSession;

    @Column(name = "requested_units", nullable = false)
    private Integer requestedUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_stage", nullable = false)
    private ApprovalStage approvalStage;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_multi_level", nullable = false)
    private Boolean isMultiLevel;

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
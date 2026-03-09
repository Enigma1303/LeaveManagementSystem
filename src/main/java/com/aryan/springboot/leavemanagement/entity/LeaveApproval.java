package com.aryan.springboot.leavemanagement.entity;

import com.aryan.springboot.leavemanagement.entity.enums.ApprovalAction;
import com.aryan.springboot.leavemanagement.entity.enums.ApprovalStage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private Employee approver;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private ApprovalStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ApprovalAction action;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "acted_at", nullable = false)
    private LocalDateTime actedAt;
}
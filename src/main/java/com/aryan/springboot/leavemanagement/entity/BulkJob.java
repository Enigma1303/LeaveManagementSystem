package com.aryan.springboot.leavemanagement.entity;

import com.aryan.springboot.leavemanagement.entity.enums.BulkJobEntity;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobStatus;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BulkJobType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity", nullable = false)
    private BulkJobEntity entity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BulkJobStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private Employee requestedBy;

    @Column(name = "file_reference")
    private String fileReference;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "successful_records")
    private Integer successfulRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
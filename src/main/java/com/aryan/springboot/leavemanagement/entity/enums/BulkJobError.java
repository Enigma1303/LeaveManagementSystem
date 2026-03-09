package com.aryan.springboot.leavemanagement.entity.enums;

import com.aryan.springboot.leavemanagement.entity.BulkJob;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_job_error")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkJobError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulk_job_id", nullable = false)
    private BulkJob bulkJob;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "raw_data", nullable = false, columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
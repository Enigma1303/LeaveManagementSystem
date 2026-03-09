package com.aryan.springboot.leavemanagement.entity;

import com.aryan.springboot.leavemanagement.entity.enums.ApiCallStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "holiday_api_call_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayApiCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApiCallStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(name = "called_at", nullable = false, updatable = false)
    private LocalDateTime calledAt;

    @PrePersist
    protected void onCreate() {
        this.calledAt = LocalDateTime.now();
    }
}
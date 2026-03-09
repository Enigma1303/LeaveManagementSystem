package com.aryan.springboot.leavemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "holiday_cache",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"country_code", "holiday_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_invalidated", nullable = false)
    private Boolean isInvalidated = false;
}
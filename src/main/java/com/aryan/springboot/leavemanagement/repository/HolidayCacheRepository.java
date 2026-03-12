package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.HolidayCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HolidayCacheRepository extends JpaRepository<HolidayCache, Long> {

    // Fetch valid (non-expired, non-invalidated) rows for a country+year
    @Query("""
            SELECT h FROM HolidayCache h
            WHERE h.countryCode = :countryCode
            AND h.year = :year
            AND h.isInvalidated = false
            AND h.expiresAt > :now
            """)
    List<HolidayCache> findValidCache(
            @Param("countryCode") String countryCode,
            @Param("year") Integer year,
            @Param("now") LocalDateTime now);

    // Invalidate all rows for a country+year (admin cache clear)
    @Modifying
    @Query("""
            UPDATE HolidayCache h
            SET h.isInvalidated = true
            WHERE h.countryCode = :countryCode
            AND h.year = :year
            """)
    void invalidateCache(
            @Param("countryCode") String countryCode,
            @Param("year") Integer year);
}
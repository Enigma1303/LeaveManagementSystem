package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.HolidayCache;
import com.aryan.springboot.leavemanagement.repository.HolidayCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HolidayCacheWriter {

    private final HolidayCacheRepository holidayCacheRepository;

    public HolidayCacheWriter(HolidayCacheRepository holidayCacheRepository) {
        this.holidayCacheRepository = holidayCacheRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToDb(Map<LocalDate, String> holidays, String isoCode,
                         int year, long cacheTtlHours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(cacheTtlHours);

        List<HolidayCache> rows = holidays.entrySet().stream()
                .map(entry -> {
                    HolidayCache cache = new HolidayCache();
                    cache.setCountryCode(isoCode);
                    cache.setYear(year);
                    cache.setHolidayDate(entry.getKey());
                    cache.setName(entry.getValue());
                    cache.setFetchedAt(now);
                    cache.setExpiresAt(expiresAt);
                    cache.setIsInvalidated(false);
                    return cache;
                })
                .toList();

        try {
            holidayCacheRepository.saveAll(rows);
            log.info("Saved {} rows to DB cache for {}/{}", rows.size(), isoCode, year);
        } catch (Exception e) {
            log.warn("Duplicate holiday cache for {}/{} — skipping: {}",
                    isoCode, year, e.getMessage());
        }
    }
}
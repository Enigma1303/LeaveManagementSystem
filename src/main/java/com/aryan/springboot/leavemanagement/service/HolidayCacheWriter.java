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

        int savedCount = 0;

        for (Map.Entry<LocalDate, String> entry : holidays.entrySet()) {

            HolidayCache cache = new HolidayCache();
            cache.setCountryCode(isoCode);
            cache.setYear(year);
            cache.setHolidayDate(entry.getKey());
            cache.setName(entry.getValue());
            cache.setFetchedAt(now);
            cache.setExpiresAt(expiresAt);
            cache.setIsInvalidated(false);

            try {
                holidayCacheRepository.save(cache);
                savedCount++;
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.debug("Duplicate holiday skipped for {}/{}/{}",
                        isoCode, year, entry.getKey());
            } catch (Exception e) {
                log.error("Failed to save holiday cache for {}/{}/{}",
                        isoCode, year, entry.getKey(), e);
                throw e;
            }
        }

        log.info("Saved {} new rows to DB cache for {}/{}", savedCount, isoCode, year);
    }
}
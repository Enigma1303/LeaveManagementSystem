package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.client.HolidayApiClient;
import com.aryan.springboot.leavemanagement.entity.HolidayCache;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import com.aryan.springboot.leavemanagement.repository.HolidayCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayApiClient holidayApiClient;
    private final HolidayCacheRepository holidayCacheRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.holiday.fallback-on-error}")
    private boolean fallbackOnError;

    @Value("${app.holiday.cache-ttl-hours:24}")
    private long cacheTtlHours;

    private static final Map<String, String> PHONE_TO_ISO = Map.ofEntries(
            Map.entry("91",  "IN"),
            Map.entry("1",   "US"),
            Map.entry("44",  "GB"),
            Map.entry("61",  "AU"),
            Map.entry("49",  "DE"),
            Map.entry("33",  "FR"),
            Map.entry("81",  "JP"),
            Map.entry("86",  "CN"),
            Map.entry("65",  "SG"),
            Map.entry("971", "AE")
    );

    public HolidayServiceImpl(HolidayApiClient holidayApiClient,
                              HolidayCacheRepository holidayCacheRepository,
                              RedisTemplate<String, Object> redisTemplate) {
        this.holidayApiClient = holidayApiClient;
        this.holidayCacheRepository = holidayCacheRepository;
        this.redisTemplate = redisTemplate;
    }

    public String resolveIsoCode(String countryCode) {
        if (countryCode.matches("\\d+")) {
            String iso = PHONE_TO_ISO.get(countryCode);
            if (iso == null) {
                throw new BusinessRuleException("Unsupported country code: " + countryCode);
            }
            return iso;
        }
        return countryCode.toUpperCase();
    }

    // Flow ye rahega: Redis → DB → Tallyfy API → save both
    @Override
    @Transactional
    public Set<LocalDate> getPublicHolidays(int year, String countryCode) {
        String isoCode = resolveIsoCode(countryCode);
        String redisKey = "holidays:" + isoCode + ":" + year;

        //  Layer 1: Redis
        Set<LocalDate> fromRedis = getFromRedis(redisKey);
        if (fromRedis != null) {
            log.info("Layer 1 HIT — Redis key: {}", redisKey);
            return fromRedis;
        }
        log.info("Layer 1 MISS — Redis key: {}", redisKey);

        //  Layer 2: DB Cache
        Set<LocalDate> fromDb = getFromDb(isoCode, year);
        if (fromDb != null) {
            log.info("Layer 2 HIT — DB cache for {}/{}", isoCode, year);
            // Warm Redis from DB so next request hits Redis
            saveToRedis(redisKey, fromDb);
            return fromDb;
        }
        log.info("Layer 2 MISS — DB cache for {}/{}", isoCode, year);

        // Layer 3: ab use karo Tallyfy API 
        try {
            Map<LocalDate, String> apiResult = holidayApiClient.fetchHolidays(isoCode, year);
            Set<LocalDate> holidayDates = apiResult.keySet();

            // Layer 4: Save to DB + Redis 
            saveToDb(apiResult, isoCode, year);
            saveToRedis(redisKey, holidayDates);

            log.info("Layer 3 HIT — API. Saved {} holidays to DB + Redis for {}/{}",
                    holidayDates.size(), isoCode, year);
            return holidayDates;

        } catch (Exception e) {
            if (fallbackOnError) {
                log.warn("All 3 layers failed for {}/{}. Fallback — treating all days as working days",
                        isoCode, year);
                return Collections.emptySet();
            }
            throw new BusinessRuleException("Holiday service unavailable. Please try again later.");
        }
    }

    @Override
    public boolean isPublicHoliday(LocalDate date, String countryCode) {
        return getPublicHolidays(date.getYear(), countryCode).contains(date);
    }

    @SuppressWarnings("unchecked")
    private Set<LocalDate> getFromRedis(String redisKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached instanceof Set) {
                return (Set<LocalDate>) cached;
            }
        } catch (Exception e) {
            // Redis down he -> go to db cache now
            log.warn("Redis unavailable during GET: {}", e.getMessage());
        }
        return null;
    }

    private void saveToRedis(String redisKey, Set<LocalDate> holidays) {
        try {
            redisTemplate.opsForValue().set(redisKey, holidays, Duration.ofHours(cacheTtlHours));
            log.info("Saved to Redis key: {} TTL: {}h", redisKey, cacheTtlHours);
        } catch (Exception e) {
            // Redis down — not critical, DB cache still works
            log.warn("Redis unavailable during SET: {}", e.getMessage());
        }
    }

    private Set<LocalDate> getFromDb(String isoCode, int year) {
        List<HolidayCache> rows = holidayCacheRepository
                .findValidCache(isoCode, year, LocalDateTime.now());
        if (rows.isEmpty()) {
            return null;
        }
        return rows.stream()
                .map(HolidayCache::getHolidayDate)
                .collect(Collectors.toSet());
    }

    private void saveToDb(Map<LocalDate, String> holidays, String isoCode, int year) {
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

        holidayCacheRepository.saveAll(rows);
        log.info("Saved {} rows to DB cache for {}/{}", rows.size(), isoCode, year);
    }
    
    // Admin cache invalidation
    // agar lets say govt declares a national holiday
    // toh stale data hatane ke liye admin ke paas ye power honi chahiye
    // Clears both Redis + DB so next request fetches fresh from API
    @Transactional
    public void invalidateCache(String countryCode, int year) {
        String isoCode = resolveIsoCode(countryCode);
        String redisKey = "holidays:" + isoCode + ":" + year;

        // Clear Redis
        try {
            redisTemplate.delete(redisKey);
            log.info("Redis cache cleared for key: {}", redisKey);
        } catch (Exception e) {
            log.warn("Redis unavailable during DELETE: {}", e.getMessage());
        }

        // Invalidate DB rows
        holidayCacheRepository.invalidateCache(isoCode, year);
        log.info("DB cache invalidated for {}/{}", isoCode, year);
    }
}
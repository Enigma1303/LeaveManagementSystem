package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.client.HolidayApiClient;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayApiClient holidayApiClient;

    @Value("${app.holiday.fallback-on-error}")
    private boolean fallbackOnError;

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

    public HolidayServiceImpl(HolidayApiClient holidayApiClient) {
        this.holidayApiClient = holidayApiClient;
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

    @Override
    public Set<LocalDate> getPublicHolidays(int year, String countryCode) {
        String isoCode = resolveIsoCode(countryCode);
        try {
            return holidayApiClient.fetchHolidays(isoCode, year);
        } catch (Exception e) {
            if (fallbackOnError) {
                log.warn("Holiday API failed. Fallback — treating all days as working days");
                return Collections.emptySet();
            }
            throw new BusinessRuleException("Holiday service unavailable. Please try again later.");
        }
    }

    @Override
    public boolean isPublicHoliday(LocalDate date, String countryCode) {
        return getPublicHolidays(date.getYear(), countryCode).contains(date);
    }
}
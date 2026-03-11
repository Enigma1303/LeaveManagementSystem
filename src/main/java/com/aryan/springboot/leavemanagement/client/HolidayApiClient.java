package com.aryan.springboot.leavemanagement.client;

import com.aryan.springboot.leavemanagement.entity.HolidayApiCallLog;
import com.aryan.springboot.leavemanagement.entity.enums.ApiCallStatus;
import com.aryan.springboot.leavemanagement.repository.HolidayApiCallLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HolidayApiClient {

    private final RestTemplate restTemplate;
    private final HolidayApiCallLogRepository holidayApiCallLogRepository;

    @Value("${app.holiday.api-url}")
    private String apiUrl;

    public HolidayApiClient(HolidayApiCallLogRepository holidayApiCallLogRepository) {
        this.restTemplate = new RestTemplate();
        this.holidayApiCallLogRepository = holidayApiCallLogRepository;
    }

    @SuppressWarnings("unchecked")
    public Set<LocalDate> fetchHolidays(String isoCode, int year) {
        String url = apiUrl + "/" + isoCode + "/" + year + ".json";
        log.info("Calling holiday API: {}", url);

        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            long responseTime = System.currentTimeMillis() - startTime;

            if (response == null || !response.containsKey("holidays")) {
                log.warn("Empty response from holiday API for {}/{}", isoCode, year);
                saveLog(isoCode, year, ApiCallStatus.SUCCESS, responseTime, null);
                return Collections.emptySet();
            }

            List<Map<String, Object>> holidays =
                    (List<Map<String, Object>>) response.get("holidays");

            Set<LocalDate> holidayDates = holidays.stream()
                    .map(h -> LocalDate.parse((String) h.get("date")))
                    .collect(Collectors.toSet());

            saveLog(isoCode, year, ApiCallStatus.SUCCESS, responseTime, null);
            log.info("Fetched {} holidays for {}/{} in {}ms",
                    holidayDates.size(), isoCode, year, responseTime);

            return holidayDates;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Holiday API call failed: {}", e.getMessage());
            saveLog(isoCode, year, ApiCallStatus.FAILURE, responseTime, e.getMessage());
            throw e;
        }
    }

    private void saveLog(String isoCode, int year, ApiCallStatus status,
                         long responseTimeMs, String errorMessage) {
        try {
            HolidayApiCallLog callLog = new HolidayApiCallLog();
            callLog.setCountryCode(isoCode);
            callLog.setYear(year);
            callLog.setStatus(status);
            callLog.setResponseTimeMs(responseTimeMs);
            callLog.setErrorMessage(errorMessage);
            holidayApiCallLogRepository.save(callLog);
        } catch (Exception e) {
            log.error("Failed to save holiday API call log: {}", e.getMessage());
        }
    }
}
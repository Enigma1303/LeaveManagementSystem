package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.HolidayApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HolidayApiCallLogRepository extends JpaRepository<HolidayApiCallLog, Long> {

    List<HolidayApiCallLog> findByCountryCodeAndYear(String countryCode, Integer year);

    List<HolidayApiCallLog> findByStatus(String status);
}
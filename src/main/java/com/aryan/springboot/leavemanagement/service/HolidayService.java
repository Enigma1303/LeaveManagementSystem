package com.aryan.springboot.leavemanagement.service;

import java.time.LocalDate;
import java.util.Set;

public interface HolidayService {
    Set<LocalDate> getPublicHolidays(int year, String countryCode);
    boolean isPublicHoliday(LocalDate date, String countryCode);
}
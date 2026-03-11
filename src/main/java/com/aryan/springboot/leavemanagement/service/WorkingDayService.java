package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.enums.Session;

import java.time.LocalDate;
import java.util.Set;

public interface WorkingDayService {
    int calculateWorkingDays(LocalDate startDate, LocalDate endDate,
                             Session startSession, Session endSession,
                             Set<LocalDate> publicHolidays);
    boolean isWorkingDay(LocalDate date, Set<LocalDate> publicHolidays);
}
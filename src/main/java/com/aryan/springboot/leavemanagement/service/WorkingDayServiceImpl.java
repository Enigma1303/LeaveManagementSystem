package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.enums.Session;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
public class WorkingDayServiceImpl implements WorkingDayService {

    @Override
    public boolean isWorkingDay(LocalDate date, Set<LocalDate> publicHolidays) {
        DayOfWeek dow = date.getDayOfWeek();
        boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
        boolean isHoliday = publicHolidays.contains(date);
        return !isWeekend && !isHoliday;
    }

    @Override
    public int calculateWorkingDays(LocalDate startDate, LocalDate endDate,
                                    Session startSession, Session endSession,
                                    Set<LocalDate> publicHolidays) {

        // Single day
        if (startDate.isEqual(endDate)) {
            if (!isWorkingDay(startDate, publicHolidays)) {
                throw new BusinessRuleException(
                        "Selected date " + startDate + " is a weekend or public holiday");
            }
            // Half day logic
            if (startSession == Session.FIRST_HALF && endSession == Session.FIRST_HALF) return 1;
            if (startSession == Session.SECOND_HALF && endSession == Session.SECOND_HALF) return 1;
            if (startSession == Session.FIRST_HALF && endSession == Session.SECOND_HALF) return 1;
            // SECOND_HALF → FIRST_HALF on same day is invalid (caught in submitLeave)
            return 1;
        }

        // Multi day
        int units = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isWorkingDay(current, publicHolidays)) {
                units++;
            }
            current = current.plusDays(1);
        }

        if (units == 0) {
            throw new BusinessRuleException(
                    "Selected date range has no working days (all weekends or public holidays)");
        }

        log.info("Calculated {} working days from {} to {}", units, startDate, endDate);
        return units;
    }
}
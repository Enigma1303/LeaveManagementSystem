package com.aryan.springboot.leavemanagement.controller;

import com.aryan.springboot.leavemanagement.service.HolidayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v2/holidays")
public class HolidayCacheController {

    private final HolidayServiceImpl holidayService;

    public HolidayCacheController(HolidayServiceImpl holidayService) {
        this.holidayService = holidayService;
    }

    // Admin clears cache agar government achanak national holiday declare karti he
    // taaki next submission se fresh data aaye from Tallyfy API
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> invalidateCache(
            @RequestParam String countryCode,
            @RequestParam int year) {

        holidayService.invalidateCache(countryCode, year);
        return ResponseEntity.ok(
                "Cache invalidated for country: " + countryCode + " year: " + year);
    }
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.*;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobStatus;
import com.aryan.springboot.leavemanagement.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

@Slf4j
@Service
public class LeaveBalanceImportProcessor {

    private final BulkJobRepository bulkJobRepository;
    private final BulkJobErrorRepository bulkJobErrorRepository;
    private final UserRepository UserRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public LeaveBalanceImportProcessor(
            BulkJobRepository bulkJobRepository,
            BulkJobErrorRepository bulkJobErrorRepository,
            UserRepository UserRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeaveBalanceRepository leaveBalanceRepository) {
        this.bulkJobRepository = bulkJobRepository;
        this.bulkJobErrorRepository = bulkJobErrorRepository;
        this.UserRepository = UserRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Async("notificationExecutor")
    public void process(Long jobId, byte[] fileBytes) {

        BulkJob job = bulkJobRepository.findById(jobId)
                .orElseThrow();

        job.setStatus(BulkJobStatus.PROCESSING);
        job.setStartedAt(java.time.LocalDateTime.now());
        bulkJobRepository.save(job);

        log.info("BulkJob {} started processing", jobId);

        int total = 0;
        int success = 0;
        int failed = 0;

        // fixed — read from byte array, never touches HTTP request
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes)))) {

            String line;
            int row = 0;

            while ((line = reader.readLine()) != null) {

                row++;
                if (row == 1) continue; // skip header

                total++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue; // skip blank lines

                try {
                    String[] parts = trimmed.split(",");

                    if (parts.length != 3) {
                        throw new RuntimeException(
                                "Expected 3 columns (email, leaveTypeName, allocatedUnits) but got " + parts.length);
                    }

                    String email           = parts[0].trim();
                    String leaveTypeName   = parts[1].trim();
                    Integer allocatedUnits = Integer.parseInt(parts[2].trim());

                    Employee employee = UserRepository
                            .findByEmailWithAuthorities(email)
                            .orElseThrow(() -> new RuntimeException("Employee not found: " + email));

                    LeaveType leaveType = leaveTypeRepository
                            .findByName(leaveTypeName)
                            .orElseThrow(() -> new RuntimeException("Leave type not found: " + leaveTypeName));

                    int year = java.time.LocalDate.now().getYear();

                    LeaveBalance balance = leaveBalanceRepository
                            .findByEmployeeIdAndLeaveTypeIdAndYear(
                                    employee.getId(), leaveType.getId(), year)
                            .orElseGet(() -> {
                                LeaveBalance newBalance = new LeaveBalance();
                                newBalance.setEmployee(employee);
                                newBalance.setLeaveType(leaveType);
                                newBalance.setYear(year);
                                newBalance.setUsedUnits(0);
                                newBalance.setPendingUnits(0);
                                return newBalance;
                            });

                    balance.setAllocatedUnits(allocatedUnits);
                    leaveBalanceRepository.save(balance);

                    success++;
                    log.info("BulkJob {} row {} — OK: {}", jobId, row, email);

                } catch (Exception ex) {
                    failed++;
                    log.warn("BulkJob {} row {} — FAILED: {}", jobId, row, ex.getMessage());

                    BulkJobError error = new BulkJobError();
                    error.setBulkJob(job);
                    error.setRowNumber(row);
                    error.setRawData(trimmed);
                    error.setErrorMessage(ex.getMessage());
                    bulkJobErrorRepository.save(error);
                }
            }

            job.setStatus(failed == 0
                    ? BulkJobStatus.COMPLETED
                    : BulkJobStatus.FAILED);

            job.setTotalRecords(total);
            job.setSuccessfulRecords(success);
            job.setFailedRecords(failed);
            job.setCompletedAt(java.time.LocalDateTime.now());

            log.info("BulkJob {} completed — total:{} success:{} failed:{}",
                    jobId, total, success, failed);

        } catch (Exception e) {
            log.error("BulkJob {} crashed: {}", jobId, e.getMessage(), e);
            job.setStatus(BulkJobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        }

        bulkJobRepository.save(job);
    }
}
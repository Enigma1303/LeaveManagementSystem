package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.*;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobStatus;
import com.aryan.springboot.leavemanagement.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
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
    @Async
    public void process(Long jobId, MultipartFile file) {

        BulkJob job = bulkJobRepository.findById(jobId)
                .orElseThrow();

        job.setStatus(BulkJobStatus.PROCESSING);
        bulkJobRepository.save(job);

        int total = 0;
        int success = 0;
        int failed = 0;

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;
            int row = 0;

            while ((line = reader.readLine()) != null) {

                row++;

                if (row == 1) continue;

                total++;

                try {

                    String[] parts = line.split(",");

                    if (parts.length != 3) {
                        throw new RuntimeException("Invalid CSV format");
                    }

                    String email = parts[0].trim();
                    String leaveTypeName = parts[1].trim();
                    Integer allocatedUnits = Integer.parseInt(parts[2].trim());

                    Employee employee = UserRepository
                            .findByEmailWithAuthorities(email)
                            .orElseThrow(() -> new RuntimeException("Employee not found"));

                    LeaveType leaveType = leaveTypeRepository
                            .findByName(leaveTypeName)
                            .orElseThrow(() -> new RuntimeException("Leave type not found"));

                    int year = java.time.LocalDate.now().getYear();

                    LeaveBalance balance = leaveBalanceRepository
                            .findByEmployeeIdAndLeaveTypeIdAndYear(
                                    employee.getId(),
                                    leaveType.getId(),
                                    year
                            )
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

                } catch (Exception ex) {

                    failed++;

                    BulkJobError error = new BulkJobError();
                    error.setBulkJob(job);
                    error.setRowNumber(row);
                    error.setRawData(line);
                    error.setErrorMessage(ex.getMessage());

                    bulkJobErrorRepository.save(error);
                }
            }

            job.setStatus(BulkJobStatus.COMPLETED);
            job.setTotalRecords(total);
            job.setSuccessfulRecords(success);
            job.setFailedRecords(failed);

        } catch (Exception e) {

            job.setStatus(BulkJobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        }

        bulkJobRepository.save(job);
    }
}
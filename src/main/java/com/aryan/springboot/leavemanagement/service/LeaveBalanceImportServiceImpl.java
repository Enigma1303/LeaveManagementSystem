package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.BulkJob;
import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobEntity;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobStatus;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobType;
import com.aryan.springboot.leavemanagement.repository.BulkJobRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LeaveBalanceImportServiceImpl implements LeaveBalanceImportService {

    private final BulkJobRepository bulkJobRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceImportProcessor importProcessor;

    public LeaveBalanceImportServiceImpl(
            BulkJobRepository bulkJobRepository,
            UserRepository userRepository,
            LeaveBalanceImportProcessor importProcessor) {
        this.bulkJobRepository = bulkJobRepository;
        this.userRepository = userRepository;
        this.importProcessor = importProcessor;
    }

    @Override
    public Long startImport(MultipartFile file, String requestedByEmail) {

        Employee admin = userRepository
                .findByEmailWithAuthorities(requestedByEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read uploaded file: " + e.getMessage());
        }

        BulkJob job = new BulkJob();
        job.setType(BulkJobType.IMPORT);
        job.setEntity(BulkJobEntity.LEAVE_BALANCE);
        job.setStatus(BulkJobStatus.QUEUED);
        job.setRequestedBy(admin);
        job.setFileReference(file.getOriginalFilename());

        BulkJob saved = bulkJobRepository.save(job);

        importProcessor.process(saved.getId(), fileBytes);

        return saved.getId();
    }
}
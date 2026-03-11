package com.aryan.springboot.leavemanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface LeaveBalanceImportService {

    Long startImport(MultipartFile file, String requestedByEmail);

}
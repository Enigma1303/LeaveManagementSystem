package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestExportService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public String exportLeaveRequestsCsv() {

        List<LeaveRequest> requests = leaveRequestRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("Employee,LeaveType,StartDate,EndDate,Status\n");

        for (LeaveRequest r : requests) {
            csv.append(r.getEmployee().getName()).append(",")
                    .append(r.getLeaveType().getName()).append(",")
                    .append(r.getStartDate()).append(",")
                    .append(r.getEndDate()).append(",")
                    .append(r.getStatus()).append("\n");
        }

        return csv.toString();
    }
}
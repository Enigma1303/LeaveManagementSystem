package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveRequestExportService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestExportService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public String exportLeaveRequestsCsv() {

        List<LeaveRequest> requests = leaveRequestRepository.findAll();

        StringBuilder csv = new StringBuilder();

        csv.append("employee_email,leave_type,start_date,end_date,status,reason\n");

        for (LeaveRequest r : requests) {

            csv.append(r.getEmployee().getEmail()).append(",");
            csv.append(r.getLeaveType().getName()).append(",");
            csv.append(r.getStartDate()).append(",");
            csv.append(r.getEndDate()).append(",");
            csv.append(r.getStatus()).append(",");
            csv.append(r.getReason()).append("\n");
        }

        return csv.toString();
    }
}
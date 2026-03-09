package com.aryan.springboot.leavemanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.request.LeaveStatusRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;

public interface LeaveService {
    LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email);
    List<LeaveViewResponse> getLeaves(String email, LeaveStatus status, Long employeeId,
            Long managerId, LocalDate startDate,
            LocalDate endDate, LocalDateTime createdAt, String search);
    LeaveStatusResponse updateLeaveStatus(Long leaveId, LeaveStatusRequest request, String email);
}
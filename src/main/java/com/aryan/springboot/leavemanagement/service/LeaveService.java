package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.request.LeaveActionRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LeaveService {

    LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email);

    Page<LeaveViewResponse> getLeaves(
            String email,
            LeaveStatus status,
            Long employeeId,
            Long managerId,
            Long leaveTypeId,
            Integer minDuration,
            Integer maxDuration,
            Boolean multiLevel,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    LeaveStatusResponse approveLeave(Long leaveId, LeaveActionRequest request, String email);

    LeaveStatusResponse rejectLeave(Long leaveId, LeaveActionRequest request, String email);

    LeaveStatusResponse cancelLeave(Long leaveId, LeaveActionRequest request, String email);
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import com.aryan.springboot.leavemanagement.request.LeaveActionRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LeaveService {

    LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email);

    List<LeaveViewResponse> getLeaves(String email, LeaveStatus status, Long employeeId,
                                      Long managerId, LocalDate startDate, LocalDate endDate,
                                      LocalDateTime createdAt, String search);

    LeaveStatusResponse approveLeave(Long leaveId, LeaveActionRequest request, String email);

    LeaveStatusResponse rejectLeave(Long leaveId, LeaveActionRequest request, String email);

    LeaveStatusResponse cancelLeave(Long leaveId, LeaveActionRequest request, String email);
}
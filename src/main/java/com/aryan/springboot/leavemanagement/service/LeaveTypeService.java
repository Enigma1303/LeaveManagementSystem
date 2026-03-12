package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LeaveTypeRequest;
import com.aryan.springboot.leavemanagement.response.LeaveTypeResponse;

import java.util.List;

public interface LeaveTypeService {

    LeaveTypeResponse createLeaveType(LeaveTypeRequest request, Long actorId);

    LeaveTypeResponse updateLeaveType(Long id, LeaveTypeRequest request, Long actorId);

    LeaveTypeResponse deactivateLeaveType(Long id, Long actorId);

    List<LeaveTypeResponse> getAllLeaveTypes();

    List<LeaveTypeResponse> getActiveLeaveTypes();

    LeaveTypeResponse getLeaveTypeById(Long id);
}
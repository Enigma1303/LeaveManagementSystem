package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LeaveRequest;
import java.util.Map;

public interface LeaveService {
    Map<String, Object> submitLeave(LeaveRequest request, String email);
}
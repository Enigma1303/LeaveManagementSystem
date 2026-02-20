package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import java.util.Map;

public interface LeaveService {
    Map<String, Object> submitLeave(LeaveSubmitRequest request, String email);
}
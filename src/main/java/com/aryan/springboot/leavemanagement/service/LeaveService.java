package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;



public interface LeaveService {
    LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email);
}
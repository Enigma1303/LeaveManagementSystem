package com.aryan.springboot.leavemanagement.service;

import java.util.List;
import com.aryan.springboot.leavemanagement.entity.Users;
import com.aryan.springboot.leavemanagement.request.LeaveStatusRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;



public interface LeaveService {
    LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email);

    List<LeaveViewResponse>getLeaves(Users user);

    LeaveStatusResponse updateLeaveStatus(Long leaveId, LeaveStatusRequest request, Users user);


}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.Users;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveHistoryResponse;
import com.aryan.springboot.leavemanagement.response.LeaveViewResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    

    private boolean hasRole(Users user, String role) {
    return user.getAuthorities().stream()
            .anyMatch(a -> a.getName().equals(role));
}


   @Override
public LeaveSubmitResponse submitLeave(LeaveSubmitRequest request, String email) {
    Users employee = userRepository.findByEmailWithAuthorities(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    LocalDate today = LocalDate.now();

    if (request.getStartDate().isBefore(today)) {
        throw new RuntimeException("Start date cannot be in the past");
    }

    if (request.getEndDate().isBefore(request.getStartDate())) {
        throw new RuntimeException("End date cannot be before start date");
    }

    LeaveRequest leave = new LeaveRequest();
    leave.setEmployee(employee);
    leave.setStartDate(request.getStartDate());
    leave.setEndDate(request.getEndDate());
    leave.setReason(request.getReason());
    leave.setStartSession(request.getStartSession());
    leave.setEndSession(request.getEndSession());

    LeaveRequest saved = leaveRequestRepository.save(leave);

    return new LeaveSubmitResponse(
        saved.getId(),
        saved.getStatus(),
        saved.getCreatedAt()
    );
}

   @Override
   public List<LeaveViewResponse> getLeaves(Users user) {

    List<LeaveRequest>leaves=new ArrayList<>();

    if(hasRole(user,"ROLE_ADMIN"))
    {
        leaves=leaveRequestRepository.findAll();
    }
    else if(hasRole(user, "ROLE_MANAGER"))
    {
        leaves.addAll(leaveRequestRepository.findByManagerId(user.getId()));
        leaves.addAll(leaveRequestRepository.findByEmployeeId(user.getId()));
    }
    else if(hasRole(user,"ROLE_EMPLOYEE"))
    {
        leaves=leaveRequestRepository.findByEmployeeId(user.getId());
    }
    else{
        throw new AccessDeniedException("User does not have a valid role to view leaves");
    }
     return leaves.stream().map(leave -> new LeaveViewResponse(
            leave.getId(),
            leave.getEmployee().getId(),
            leave.getEmployee().getName(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getStartSession(),
            leave.getEndSession(),
            leave.getReason(),
            leave.getStatus(),
            leave.getCreatedAt(),
            leave.getStatusHistory().stream().map(h -> new LeaveHistoryResponse(
                h.getOldStatus(),
                h.getNewStatus(),
                h.getComment(),
                h.getCreatedBy().getName(),
                h.getCreatedAt()
            )).toList()
        )).toList();

   }
}
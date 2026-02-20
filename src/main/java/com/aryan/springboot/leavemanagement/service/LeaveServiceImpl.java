package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.Users;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> submitLeave(LeaveSubmitRequest request, String email) {
        Users employee = userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStartSession(request.getStartSession());
        leave.setEndSession(request.getEndSession());

        LeaveRequest saved = leaveRequestRepository.save(leave);

        return Map.of(
            "id", saved.getId(),
            "employeeId", employee.getId(),
            "startDate", saved.getStartDate(),
            "endDate", saved.getEndDate(),
            "reason", saved.getReason(),
            "startSession", saved.getStartSession(),
            "endSession", saved.getEndSession(),
            "status", saved.getStatus(),
            "createdAt", saved.getCreatedAt()
        );
    }
}
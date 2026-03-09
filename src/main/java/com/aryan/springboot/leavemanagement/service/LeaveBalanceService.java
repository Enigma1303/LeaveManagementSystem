package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.response.LeaveBalanceResponse;

import java.util.List;

public interface LeaveBalanceService {


    List<LeaveBalanceResponse> getMyBalances(String email, Integer year);


    List<LeaveBalanceResponse> getAllBalances(Integer year);


    List<LeaveBalanceResponse> getBalancesByEmployeeId(Long employeeId, Integer year);

    void lockPendingUnits(Long employeeId, Long leaveTypeId, Integer year, Integer units);

    void releasePendingUnits(Long employeeId, Long leaveTypeId, Integer year, Integer units);

    void deductOnApproval(Long employeeId, Long leaveTypeId, Integer year, Integer units);

    void checkAvailableBalance(Long employeeId, Long leaveTypeId, Integer year, Integer requestedUnits);
}
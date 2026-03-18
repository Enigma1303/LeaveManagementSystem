package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveBalance;
import com.aryan.springboot.leavemanagement.repository.LeaveBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeaveBalanceExportService {

    private final LeaveBalanceRepository leaveBalanceRepository;

    public LeaveBalanceExportService(LeaveBalanceRepository leaveBalanceRepository) {
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Transactional(readOnly = true)
    public String exportLeaveBalancesToCsv() {

        List<LeaveBalance> balances = leaveBalanceRepository.findAll();

        StringBuilder csv = new StringBuilder();

        csv.append("employee_email,leave_type,year,allocated_units,used_units,pending_units\n");

        for (LeaveBalance lb : balances) {

            csv.append(lb.getEmployee().getEmail()).append(",");
            csv.append(lb.getLeaveType().getName()).append(",");
            csv.append(lb.getYear()).append(",");
            csv.append(lb.getAllocatedUnits()).append(",");
            csv.append(lb.getUsedUnits()).append(",");
            csv.append(lb.getPendingUnits()).append("\n");
        }

        return csv.toString();
    }
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.entity.LeaveBalance;
import com.aryan.springboot.leavemanagement.entity.enums.AuditAction;
import com.aryan.springboot.leavemanagement.entity.enums.AuditEntityType;
import com.aryan.springboot.leavemanagement.exception.BusinessRuleException;
import com.aryan.springboot.leavemanagement.exception.ResourceNotFoundException;
import com.aryan.springboot.leavemanagement.repository.LeaveBalanceRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.response.LeaveBalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService; 

    public LeaveBalanceServiceImpl(LeaveBalanceRepository leaveBalanceRepository,
                                   UserRepository userRepository,
                                   AuditService auditService) { 
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
        this.auditService = auditService; 
    }

    private LeaveBalanceResponse toResponse(LeaveBalance lb) {
        return new LeaveBalanceResponse(
                lb.getId(),
                lb.getEmployee().getId(),
                lb.getEmployee().getName(),
                lb.getLeaveType().getId(),
                lb.getLeaveType().getName(),
                lb.getYear(),
                lb.getAllocatedUnits(),
                lb.getUsedUnits(),
                lb.getPendingUnits(),
                lb.getAvailableUnits(),
                lb.getCreatedAt(),
                lb.getUpdatedAt()
        );
    }

    private LeaveBalance getBalance(Long employeeId, Long leaveTypeId, Integer year) {
        return leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No leave balance found for employeeId=" + employeeId
                                + ", leaveTypeId=" + leaveTypeId + ", year=" + year));
    }

    // Earlier i had missed transactional which resulted in LazyInitializationException
    // due to accessing a lazily loaded entity outside the session.
    @Transactional(readOnly = true)
    @Override
    public List<LeaveBalanceResponse> getMyBalances(String email, Integer year) {
        Employee employee = userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
        int targetYear = (year != null) ? year : java.time.Year.now().getValue();
        log.info("Fetching balances for employee: {} year: {}", email, targetYear);
        return leaveBalanceRepository
                .findByEmployeeIdAndYear(employee.getId(), targetYear)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<LeaveBalanceResponse> getAllBalances(Integer year) {
        int targetYear = (year != null) ? year : java.time.Year.now().getValue();
        log.info("Admin fetching all balances for year: {}", targetYear);
        return leaveBalanceRepository.findAllByYear(targetYear)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<LeaveBalanceResponse> getBalancesByEmployeeId(Long employeeId, Integer year) {
        int targetYear = (year != null) ? year : java.time.Year.now().getValue();
        log.info("Fetching balances for employeeId: {} year: {}", employeeId, targetYear);
        return leaveBalanceRepository
                .findByEmployeeIdAndYear(employeeId, targetYear)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public void checkAvailableBalance(Long employeeId, Long leaveTypeId, Integer year,
                                      Integer requestedUnits) {
        LeaveBalance balance = getBalance(employeeId, leaveTypeId, year);
        if (balance.getAvailableUnits() < requestedUnits) {
            log.warn("Insufficient balance for employeeId={} - available={} requested={}",
                    employeeId, balance.getAvailableUnits(), requestedUnits);
            throw new BusinessRuleException(
                    "Insufficient leave balance. Available: " + balance.getAvailableUnits()
                            + ", Requested: " + requestedUnits);
        }
    }

    @Transactional
    @Override
    public void lockPendingUnits(Long employeeId, Long leaveTypeId, Integer year, Integer units) {
        LeaveBalance balance = getBalance(employeeId, leaveTypeId, year);
        balance.setPendingUnits(balance.getPendingUnits() + units);
        leaveBalanceRepository.save(balance);
        log.info("Locked {} pending units for employeeId={}", units, employeeId);
    }

    @Transactional
    @Override
    public void releasePendingUnits(Long employeeId, Long leaveTypeId, Integer year, Integer units) {
        LeaveBalance balance = getBalance(employeeId, leaveTypeId, year);
        balance.setPendingUnits(Math.max(0, balance.getPendingUnits() - units));
        leaveBalanceRepository.save(balance);
        log.info("Released {} pending units for employeeId={}", units, employeeId);
    }

    @Transactional
    @Override
    public void restoreUsedUnits(Long employeeId, Long leaveTypeId, Integer year, Integer units) {
        LeaveBalance balance = getBalance(employeeId, leaveTypeId, year);
        balance.setUsedUnits(Math.max(0, balance.getUsedUnits() - units));
        leaveBalanceRepository.save(balance);
        log.info("Restored {} used units on cancellation for employeeId={}", units, employeeId);
    }

    @Transactional
    @Override
    public void deductOnApproval(Long employeeId, Long leaveTypeId, Integer year, Integer units) {
        LeaveBalance balance = getBalance(employeeId, leaveTypeId, year);
        balance.setPendingUnits(Math.max(0, balance.getPendingUnits() - units));
        balance.setUsedUnits(balance.getUsedUnits() + units);
        leaveBalanceRepository.save(balance);
        log.info("Deducted {} units on approval for employeeId={}", units, employeeId);
    }
    
    @Transactional
    @Override
    public LeaveBalanceResponse adjustBalance(Long balanceId, Integer newAllocated, Long actorId) { 
        LeaveBalance balance = leaveBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave balance not found for id: " + balanceId));

        String oldValue = String.valueOf(balance.getAllocatedUnits()); 
        balance.setAllocatedUnits(newAllocated);
        LeaveBalance saved = leaveBalanceRepository.save(balance);

        auditService.log(actorId, AuditEntityType.LEAVE_BALANCE, saved.getId(),
                AuditAction.UPDATE, oldValue, String.valueOf(newAllocated));

        log.info("Balance id:{} adjusted from {} to {} by actor:{}",
                balanceId, oldValue, newAllocated, actorId);
        return toResponse(saved);
    }
}
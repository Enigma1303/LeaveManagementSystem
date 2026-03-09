package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.LeaveType;
import com.aryan.springboot.leavemanagement.repository.LeaveTypeRepository;
import com.aryan.springboot.leavemanagement.request.LeaveTypeRequest;
import com.aryan.springboot.leavemanagement.response.LeaveTypeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeServiceImpl(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    private void validateRequest(LeaveTypeRequest request) {
        if (Boolean.TRUE.equals(request.getIsMultiLevelApproval())
                && request.getMultiLevelTriggerUnits() == null) {
            throw new RuntimeException(
                    "multiLevelTriggerUnits is required when isMultiLevelApproval is true");
        }
        if (Boolean.FALSE.equals(request.getIsMultiLevelApproval())
                && request.getMultiLevelTriggerUnits() != null) {
            throw new RuntimeException(
                    "multiLevelTriggerUnits must not be set when isMultiLevelApproval is false");
        }
    }

    private LeaveTypeResponse toResponse(LeaveType lt) {
        return new LeaveTypeResponse(
                lt.getId(),
                lt.getName(),
                lt.getMaxUnitsPerRequest(),
                lt.getMinAdvanceNoticeDays(),
                lt.getIsMultiLevelApproval(),
                lt.getMultiLevelTriggerUnits(),
                lt.getReminderThresholdDays(),
                lt.getIsActive(),
                lt.getCreatedAt(),
                lt.getUpdatedAt()
        );
    }

    @Transactional
    @Override
    public LeaveTypeResponse createLeaveType(LeaveTypeRequest request) {
        log.info("Creating leave type: {}", request.getName());

        if (leaveTypeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Leave type with name '" + request.getName() + "' already exists");
        }

        validateRequest(request);

        LeaveType leaveType = new LeaveType();
        leaveType.setName(request.getName());
        leaveType.setMaxUnitsPerRequest(request.getMaxUnitsPerRequest());
        leaveType.setMinAdvanceNoticeDays(request.getMinAdvanceNoticeDays());
        leaveType.setIsMultiLevelApproval(request.getIsMultiLevelApproval());
        leaveType.setMultiLevelTriggerUnits(request.getMultiLevelTriggerUnits());
        leaveType.setReminderThresholdDays(request.getReminderThresholdDays());
        leaveType.setIsActive(true);

        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Leave type created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    @Override
    public LeaveTypeResponse updateLeaveType(Long id, LeaveTypeRequest request) {
        log.info("Updating leave type id: {}", id);

        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found for id: " + id));

        // Okay so -> Allow name update only if not taken by another record
        if (!leaveType.getName().equals(request.getName())
                && leaveTypeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Leave type with name '" + request.getName() + "' already exists");
        }

        validateRequest(request);

        leaveType.setName(request.getName());
        leaveType.setMaxUnitsPerRequest(request.getMaxUnitsPerRequest());
        leaveType.setMinAdvanceNoticeDays(request.getMinAdvanceNoticeDays());
        leaveType.setIsMultiLevelApproval(request.getIsMultiLevelApproval());
        leaveType.setMultiLevelTriggerUnits(request.getMultiLevelTriggerUnits());
        leaveType.setReminderThresholdDays(request.getReminderThresholdDays());

        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Leave type updated: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    @Override
    public LeaveTypeResponse deactivateLeaveType(Long id) {
        log.info("Deactivating leave type id: {}", id);

        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found for id: " + id));

        leaveType.setIsActive(false);
        LeaveType saved = leaveTypeRepository.save(leaveType);
        log.info("Leave type deactivated: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<LeaveTypeResponse> getActiveLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrue().stream().map(this::toResponse).toList();
    }

    @Override
    public LeaveTypeResponse getLeaveTypeById(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found for id: " + id));
        return toResponse(leaveType);
    }
}
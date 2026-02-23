package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.*;
import com.aryan.springboot.leavemanagement.repository.LeaveRequestRepository;
import com.aryan.springboot.leavemanagement.repository.LeaveStatusHistoryRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LeaveStatusRequest;
import com.aryan.springboot.leavemanagement.request.LeaveSubmitRequest;
import com.aryan.springboot.leavemanagement.response.LeaveStatusResponse;
import com.aryan.springboot.leavemanagement.response.LeaveSubmitResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private LeaveStatusHistoryRepository leaveStatusHistoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Users employee;
    private Users manager;
    private LeaveRequest leaveRequest;
    private LeaveSubmitRequest submitRequest;

    @BeforeEach
    void setUp() {
        manager = new Users();
        manager.setId(1L);
        manager.setName("Manager");
        manager.setEmail("manager@example.com");
        Set<Authority> managerRoles = new HashSet<>();
        Authority managerRole = new Authority();
        managerRole.setName("ROLE_MANAGER");
        managerRoles.add(managerRole);
        manager.setAuthorities(managerRoles);

        employee = new Users();
        employee.setId(2L);
        employee.setName("Employee");
        employee.setEmail("employee@example.com");
        employee.setManager(manager);
        Set<Authority> employeeRoles = new HashSet<>();
        Authority employeeRole = new Authority();
        employeeRole.setName("ROLE_EMPLOYEE");
        employeeRoles.add(employeeRole);
        employee.setAuthorities(employeeRoles);

        leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(LocalDate.now().plusDays(5));
        leaveRequest.setEndDate(LocalDate.now().plusDays(7));
        leaveRequest.setReason("Personal work");
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setStatusHistory(new HashSet<>());

        submitRequest = new LeaveSubmitRequest();
        submitRequest.setStartDate(LocalDate.now().plusDays(5));
        submitRequest.setEndDate(LocalDate.now().plusDays(7));
        submitRequest.setStartSession(SessionType.FIRST_HALF);
        submitRequest.setEndSession(SessionType.SECOND_HALF);
        submitRequest.setReason("Personal work");
    }


    @Test
    void submitLeave_success() {
        when(userRepository.findByEmailWithAuthorities(employee.getEmail()))
                .thenReturn(Optional.of(employee));
        when(leaveRequestRepository.countOverlappingLeaves(any(), any(), any()))
                .thenReturn(0L);
        when(leaveRequestRepository.save(any())).thenAnswer(inv -> {
            LeaveRequest l = inv.getArgument(0);
            l.setId(1L);
            l.setStatus(LeaveStatus.PENDING);
            l.setCreatedAt(LocalDateTime.now());
            return l;
        });

        LeaveSubmitResponse response = leaveService.submitLeave(submitRequest, employee.getEmail());

        assertNotNull(response);
        assertEquals(LeaveStatus.PENDING, response.getStatus());
        verify(leaveRequestRepository, times(1)).save(any());
    }

   
    @Test
    void submitLeave_pastDate_throwsException() {
        when(userRepository.findByEmailWithAuthorities(employee.getEmail()))
                .thenReturn(Optional.of(employee));
        submitRequest.setStartDate(LocalDate.now().minusDays(1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> leaveService.submitLeave(submitRequest, employee.getEmail()));

        assertEquals("Start date cannot be in the past", ex.getMessage());
        verify(leaveRequestRepository, never()).save(any());
    }

   
    @Test
    void updateLeaveStatus_managerApprovesSuccess() {
        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any())).thenReturn(leaveRequest);
        when(leaveStatusHistoryRepository.save(any())).thenReturn(null);

        LeaveStatusRequest statusRequest = new LeaveStatusRequest();
        statusRequest.setStatus(LeaveStatus.APPROVED);
        statusRequest.setComment("Approved!");

        LeaveStatusResponse response = leaveService.updateLeaveStatus(1L, statusRequest, manager);

        assertNotNull(response);
        assertEquals(LeaveStatus.APPROVED, response.getStatus());
        verify(leaveStatusHistoryRepository, times(1)).save(any());
    }


    @Test
    void updateLeaveStatus_employeeTriesToUpdate_throwsAccessDenied() {
        LeaveStatusRequest statusRequest = new LeaveStatusRequest();
        statusRequest.setStatus(LeaveStatus.APPROVED);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> leaveService.updateLeaveStatus(1L, statusRequest, employee));

        assertEquals("Only managers and admins can update leave statuses", ex.getMessage());
        verify(leaveRequestRepository, never()).findById(any());
    }
}
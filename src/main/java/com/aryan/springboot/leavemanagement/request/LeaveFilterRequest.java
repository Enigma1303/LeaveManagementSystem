package com.aryan.springboot.leavemanagement.request;

import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveFilterRequest {

    private Long employeeId;
    private Long managerId;
    private LeaveStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer page = 0;
    private Integer size = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
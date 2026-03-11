package com.aryan.springboot.leavemanagement.repository.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.entity.enums.LeaveStatus;

public class LeaveRequestSpecification {

    public static Specification<LeaveRequest> employee(Long employeeId) {
        return (root, query, cb) ->
                employeeId == null ? null :
                        cb.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<LeaveRequest> manager(Long managerId) {
        return (root, query, cb) ->
                managerId == null ? null :
                        cb.equal(root.get("employee").get("manager").get("id"), managerId);
    }

    public static Specification<LeaveRequest> status(LeaveStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<LeaveRequest> startDate(LocalDate startDate) {
        return (root, query, cb) ->
                startDate == null ? null :
                        cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    public static Specification<LeaveRequest> endDate(LocalDate endDate) {
        return (root, query, cb) ->
                endDate == null ? null :
                        cb.lessThanOrEqualTo(root.get("endDate"), endDate);
    }

}
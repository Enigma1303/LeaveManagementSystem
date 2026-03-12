package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.LeaveRequest;
import com.aryan.springboot.leavemanagement.projection.MetricsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MetricsRepository extends JpaRepository<LeaveRequest, Long> {

    @Query(value = """
        SELECT
            (SELECT COUNT(*) FROM leave_request l 
                WHERE l.created_at >= :start) AS totalLeaveRequests,

            (SELECT COUNT(*) FROM leave_request l 
                WHERE l.status = 'APPROVED'
                AND l.created_at >= :start) AS totalApprovedLeaves,

            (SELECT COUNT(*) FROM leave_request l 
                WHERE l.status = 'REJECTED'
                AND l.created_at >= :start) AS totalRejectedLeaves,

            (SELECT AVG(TIMESTAMPDIFF(HOUR, l.created_at, l.updated_at))
                FROM leave_request l
                WHERE l.status = 'APPROVED'
                AND l.updated_at >= :start) AS avgApprovalTimeHours,

            (SELECT COUNT(*) FROM holiday_api_call_log h 
                WHERE h.called_at >= :start) AS totalHolidayApiCalls,

            (SELECT COUNT(*) FROM notification_log n 
                WHERE n.sent_at >= :start) AS totalNotificationsSent
        """, nativeQuery = true)
    MetricsProjection getMetrics(@Param("start") LocalDateTime start);
}
package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.NotificationLog;
import com.aryan.springboot.leavemanagement.entity.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByStatus(NotificationStatus status);
    List<NotificationLog> findByStatusAndAttemptCountLessThan(
            NotificationStatus status,
            int maxAttempts
    );
    @Query("SELECT n FROM NotificationLog n WHERE n.leaveRequest.id = :leaveRequestId")
    List<NotificationLog> findByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.service.NotificationDto;

public interface NotificationService {
    void notifyLeaveSubmitted(NotificationDto dto);
    void notifyLeaveApproved(NotificationDto dto);
    void notifyLeaveRejected(NotificationDto dto);
    void notifyLeaveCancelled(NotificationDto dto);
    void notifyManagerApproved(NotificationDto dto);
    void notifyAdminPendingApproval(NotificationDto dto);
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.service.NotificationDto;

public interface NotificationService {
    void notifyLeaveSubmitted(NotificationDto dto);
    void notifyLeaveApproved(NotificationDto dto);
    void notifyLeaveRejected(NotificationDto dto);
    //void notifyLeaveCancelled(NotificationDto dto);
    // splitting because agar multilevel leave cancel ho rhi thi toh delay aa raha tha manager aur admin ke mails me
    void notifyLeaveCancelledManager(NotificationDto dto);
    void notifyLeaveCancelledAdmin(NotificationDto dto);
    void notifyManagerApproved(NotificationDto dto);
    void notifyAdminPendingApproval(NotificationDto dto);
}
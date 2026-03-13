package com.aryan.springboot.leavemanagement.response;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Boolean isActive;
    private Long managerId;
    private LocalDateTime createdAt;

    public UserResponse(Long id, String fullName, String email,
                        Boolean isActive, Long managerId, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.isActive = isActive;
        this.managerId = managerId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Boolean getIsActive() { return isActive; }
    public Long getManagerId() { return managerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
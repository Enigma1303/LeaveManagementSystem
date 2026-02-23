package com.aryan.springboot.leavemanagement.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterResponse {

    private Long id;
    private String name;
    private String email;
    private Long managerId;
    private List<String> roles;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public RegisterResponse(Long id, String name, String email, Long managerId, List<String> roles, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.managerId = managerId;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Long getManagerId() { return managerId; }
    public List<String> getRoles() { return roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
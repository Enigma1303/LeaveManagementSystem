package com.aryan.springboot.leavemanagement.response;

public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn = 9600;
    private String role;

    public LoginResponse(String accessToken, String role) {
        this.accessToken = accessToken;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public String getRole() { return role; }
}
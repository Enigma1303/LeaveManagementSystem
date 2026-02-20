package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LoginRequest;
import com.aryan.springboot.leavemanagement.request.RegisterRequest;

public interface AuthService {

    Object register(RegisterRequest request);

    String login(LoginRequest request);
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.request.LoginRequest;
import com.aryan.springboot.leavemanagement.request.RegisterRequest;
import com.aryan.springboot.leavemanagement.response.LoginResponse;
import com.aryan.springboot.leavemanagement.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
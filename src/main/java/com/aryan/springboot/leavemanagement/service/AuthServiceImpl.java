package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Authority;
import com.aryan.springboot.leavemanagement.entity.Employee;
import com.aryan.springboot.leavemanagement.repository.AuthorityRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LoginRequest;
import com.aryan.springboot.leavemanagement.request.RegisterRequest;
import com.aryan.springboot.leavemanagement.response.LoginResponse;
import com.aryan.springboot.leavemanagement.response.RegisterResponse;
import com.aryan.springboot.leavemanagement.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           AuthorityRepository authorityRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registration requested for email: {}", request.getEmail());

        Set<Authority> roles = request.getRoles()
                .stream()
                .map(roleName -> authorityRepository.findByName(roleName)
                        .orElseThrow(() -> {
                            log.error("Role not found: {}", roleName);
                            return new RuntimeException("Role not found: " + roleName);
                        }))
                .collect(Collectors.toSet());

        Employee user = new Employee(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        user.setAuthorities(roles);

        if (request.getManagerId() != null) {
            Employee manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> {
                        log.error("Manager not found for id: {}", request.getManagerId());
                        return new RuntimeException("Manager not found");
                    });
            user.setManager(manager);
            log.info("Manager with id: {} assigned to user: {}", request.getManagerId(), request.getEmail());
        }

        Employee saved = userRepository.save(user);
        log.info("User registered successfully - id: {}, email: {}, roles: {}",
                saved.getId(), saved.getEmail(), roles.stream().map(Authority::getName).toList());

        return new RegisterResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getManager() != null ? saved.getManager().getId() : null,
                roles.stream().map(Authority::getName).toList(),
                saved.getCreatedAt()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw e;
        }

        Employee user = userRepository.findByEmailWithAuthorities(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found after authentication for email: {}", request.getEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        CustomUserDetails userDetails = new CustomUserDetails(user);

        var claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getAuthorities()
                .stream()
                .map(Authority::getName)
                .toList());

        String token = jwtService.generateToken(claims, userDetails);
        String role = user.getAuthorities().stream()
                .map(Authority::getName)
                .findFirst()
                .orElse("ROLE_EMPLOYEE");

        log.info("Login successful for email: {} with role: {}", request.getEmail(), role);
        return new LoginResponse(token, role);
    }
}
package com.aryan.springboot.leavemanagement.service;

import com.aryan.springboot.leavemanagement.entity.Authority;
import com.aryan.springboot.leavemanagement.entity.Users;
import com.aryan.springboot.leavemanagement.repository.AuthorityRepository;
import com.aryan.springboot.leavemanagement.repository.UserRepository;
import com.aryan.springboot.leavemanagement.request.LoginRequest;
import com.aryan.springboot.leavemanagement.request.RegisterRequest;
import com.aryan.springboot.leavemanagement.response.LoginResponse;
import com.aryan.springboot.leavemanagement.response.RegisterResponse;
import com.aryan.springboot.leavemanagement.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

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
    Set<Authority> roles = request.getRoles()
            .stream()
            .map(roleName -> authorityRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
            .collect(Collectors.toSet());

    Users user = new Users(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword())
    );
    user.setAuthorities(roles);

    if (request.getManagerId() != null) {
        Users manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        user.setManager(manager);
    }

    Users saved = userRepository.save(user);

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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Users user = userRepository.findByEmailWithAuthorities(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        var claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        claims.put("roles", user.getAuthorities()
                .stream()
                .map(Authority::getName)
                .toList());

        String token =jwtService.generateToken(claims, userDetails);
        String role=user.getAuthorities().stream().map(Authority::getName).findFirst().orElse("Role Employee");
        return  new LoginResponse(token,role);
    }
}
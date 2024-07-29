package com.groupthree.lms.controller;

import com.example.lms.dto.*;
import com.groupthree.lms.dto.AuthResponse;
import com.groupthree.lms.dto.SignUpRequest;
import com.groupthree.lms.exception.EmailAlreadyExistsException;
import com.groupthree.lms.exception.UserNotFoundException;
import com.groupthree.lms.exception.UsernameAlreadyExistsException;
import com.groupthree.lms.model.Role;
import com.groupthree.lms.model.User;
import com.groupthree.lms.security.JwtTokenProvider;
import com.groupthree.lms.service.EmailSenderService;
import com.groupthree.lms.service.UserService;
import com.groupthree.lms.dto.ApiResponse;
import com.groupthree.lms.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailSenderService emailSenderService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Check if the login is via email or username
        String loginField = loginRequest.getLogin();
        String password = loginRequest.getPassword();

        User user = userService.findByUsernameOrEmail(loginField, loginField)
                .orElseThrow(() -> new UserNotFoundException("User not found with this username or email"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                jwt,
                tokenProvider.getJwtExpirationInMs()
        );

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken!");
        }

        if (userService.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email Address already in use!");
        }

        // Creating user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());
        user.setRoles(Set.of(Role.valueOf(signUpRequest.getRole().toUpperCase())));

        User result = userService.registerUser(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        // Send welcome email
        emailSenderService.sendSimpleEmail(result.getEmail(), "Welcome to Our Service", result.getUsername());

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}

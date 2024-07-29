package com.example.lms.controller;

import com.example.lms.dto.ApiResponse;
import com.example.lms.dto.AuthResponse;
import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.SignUpRequest;
import com.example.lms.exception.EmailAlreadyExistsException;
import com.example.lms.exception.UserNotFoundException;
import com.example.lms.exception.UsernameAlreadyExistsException;
import com.example.lms.model.Role;
import com.example.lms.model.User;
import com.example.lms.security.JwtTokenProvider;
import com.example.lms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthController authController;


    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void authenticateUser_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Authentication authentication = mock(Authentication.class);
        when(userService.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("test-token");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(3600000);

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals(user.getId(), authResponse.getId());
        assertEquals(user.getUsername(), authResponse.getUsername());
        assertEquals(user.getEmail(), authResponse.getEmail());
        assertEquals("test-token", authResponse.getToken());
        assertEquals(3600000L, authResponse.getExpirationTime());
    }

    @Test
    void authenticateUser_UserNotFound() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("nonexistent");
        loginRequest.setPassword("password");

        when(userService.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> authController.authenticateUser(loginRequest));
    }

    @Test
    void registerUser_Success() {
        // Arrange
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "password";
        String role = "STUDENT";

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(username);
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        signUpRequest.setRole(role);

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRoles(Set.of(Role.valueOf(role)));

        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenReturn(newUser);

        // Act
        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertTrue(apiResponse.getSuccess());
        assertEquals("User registered successfully", apiResponse.getMessage());

        verify(userService).registerUser(argThat(user ->
                user.getUsername().equals(username) &&
                        user.getEmail().equals(email) &&
                        user.getPassword().equals(password) &&
                        user.getRoles().equals(Set.of(Role.valueOf(role)))
        ));
    }
    @Test
    void registerUser_UsernameAlreadyExists() {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("existinguser");
        signUpRequest.setEmail("new@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setRole("STUDENT");

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> authController.registerUser(signUpRequest));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("existing@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setRole("USER");

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authController.registerUser(signUpRequest));
    }
}
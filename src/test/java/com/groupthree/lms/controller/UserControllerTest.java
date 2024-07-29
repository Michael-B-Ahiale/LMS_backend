package com.groupthree.lms.controller;

import com.groupthree.lms.model.Role;
import com.groupthree.lms.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setEmail("test@example.com");
        currentUser.setRoles(Set.of(Role.STUDENT));

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(currentUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, Object> userDetails = (Map<String, Object>) response.getBody();
        assertEquals("testuser", userDetails.get("username"));
        assertEquals("test@example.com", userDetails.get("email"));
        assertEquals(Set.of(Role.STUDENT), userDetails.get("roles"));
    }

    @Test
    void getCurrentUser_NoAuthenticatedUser() {
        // Act
        ResponseEntity<?> response = userController.getCurrentUser(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No authenticated user found", response.getBody());
    }
}
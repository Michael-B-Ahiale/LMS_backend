package com.groupthree.lms.security;

import com.groupthree.lms.exception.UserNotFoundException;
import com.groupthree.lms.model.Role;
import com.groupthree.lms.model.User;
import com.groupthree.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_Success() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setRoles(Set.of(Role.ADMIN));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));
    }

    @Test
    void loadUserEntityByUsername_Success() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setRoles(Set.of(Role.ADMIN));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        User resultUser = userDetailsService.loadUserEntityByUsername(username);

        // Assert
        assertNotNull(resultUser);
        assertEquals(username, resultUser.getUsername());
        assertEquals("password", resultUser.getPassword());
        assertEquals(Set.of(Role.ADMIN), resultUser.getRoles());
    }

    @Test
    void loadUserEntityByUsername_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userDetailsService.loadUserEntityByUsername(username));
    }
}
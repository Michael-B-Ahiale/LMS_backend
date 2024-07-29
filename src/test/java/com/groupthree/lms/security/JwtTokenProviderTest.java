package com.groupthree.lms.security;

import com.groupthree.lms.exception.InvalidJwtTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKeya_very_long_and__long_1234567890TestSecretKeyTestSecretKeyTestSecretKey");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", 3600000);
    }

    @Test
    void generateToken_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        User principal = new User("testuser", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void getUsernameFromJWT_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        User principal = new User("testuser", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String username = jwtTokenProvider.getUsernameFromJWT(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_ValidToken() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        User principal = new User("testuser", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);
        String token = jwtTokenProvider.generateToken(authentication);

        // Act & Assert
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalidToken",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    })
    void validateToken_InvalidToken(String invalidToken) {
        // Act & Assert
        assertThrows(InvalidJwtTokenException.class, () -> jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void getJwtExpirationInMs_ReturnsCorrectValue() {
        // Act
        int expirationInMs = jwtTokenProvider.getJwtExpirationInMs();

        // Assert
        assertEquals(3600000, expirationInMs);
    }
}
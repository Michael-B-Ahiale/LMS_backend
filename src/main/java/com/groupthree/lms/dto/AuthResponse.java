package com.groupthree.lms.dto;

public class AuthResponse {

    private Long id;
    private final String username;
    private String email;
    private String token;
    private long expirationTime;

    public AuthResponse(Long id, String email,String username, String token, long expirationTime) {
        this.id = id;
        this.email = email;
        this.username=username;
        this.token = token;
        this.expirationTime = expirationTime;
    }

    public String getUsername() {
        return username;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
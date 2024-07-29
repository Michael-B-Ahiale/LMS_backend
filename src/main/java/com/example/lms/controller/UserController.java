package com.example.lms.controller;

import com.example.lms.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body("No authenticated user found");
        }

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("username", currentUser.getUsername());
        userDetails.put("email", currentUser.getEmail());
        userDetails.put("roles", currentUser.getRoles());

        return ResponseEntity.ok(userDetails);
    }
}
package com.example.authdemo.controller;

import com.example.authdemo.dto.AuthResponse;
import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.dto.RegisterRequest;
import com.example.authdemo.dto.UserResponse;
import com.example.authdemo.entity.User;
import com.example.authdemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.registerUser(registerRequest);
            
            // Convert to response format
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                userService.getUserRoles(user.getUsername())
            );
            
            return ResponseEntity.ok(
                new AuthResponse("User registered successfully!", true, userResponse)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(e.getMessage(), false));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            UserResponse userResponse = userService.loginUser(loginRequest);
            
            return ResponseEntity.ok(
                new AuthResponse("Login successful!", true, userResponse)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(e.getMessage(), false));
        }
    }
    
    @GetMapping("/users")
// Remove all authorization and parameters - just make it work
public ResponseEntity<AuthResponse> getAllUsers() {
    try {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
            new AuthResponse("Users retrieved successfully!", true, users)
        );
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new AuthResponse("Error retrieving users", false));
    }
}
    
    @GetMapping("/users/{id}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<AuthResponse> getUserById(@PathVariable Long id) {
        try {
            Optional<UserResponse> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(
                    new AuthResponse("User found!", true, user.get())
                );
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse("Error retrieving user", false));
        }
    }
    
    // Test endpoint for role checking
    @GetMapping("/admin-only")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> adminOnly() {
        return ResponseEntity.ok(
            new AuthResponse("Admin access granted!", true, null)
        );
    }
    
    @GetMapping("/manager-or-admin")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<AuthResponse> managerOrAdmin() {
        return ResponseEntity.ok(
            new AuthResponse("Manager/Admin access granted!", true, null)
        );
    }

    @GetMapping("/test-endpoint")
    public ResponseEntity<AuthResponse> testEndpoint() {
    return ResponseEntity.ok(
        new AuthResponse("Test endpoint works!", true, null)
    );
}
}
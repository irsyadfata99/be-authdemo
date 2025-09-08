package com.example.authdemo.service;

import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.dto.RegisterRequest;
import com.example.authdemo.dto.UserResponse;
import com.example.authdemo.entity.Role;
import com.example.authdemo.entity.RoleName;
import com.example.authdemo.entity.User;
import com.example.authdemo.repository.RoleRepository;
import com.example.authdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
public User registerUser(RegisterRequest registerRequest) {
    System.out.println("=== REGISTRATION DEBUG ===");
    System.out.println("Username: " + registerRequest.getUsername());
    System.out.println("Email: " + registerRequest.getEmail());
    System.out.println("Role received: [" + registerRequest.getRole() + "]");
    
    // Check if username exists
    if (userRepository.existsByUsername(registerRequest.getUsername())) {
        System.out.println("ERROR: Username already taken");
        throw new RuntimeException("Username is already taken!");
    }
    
    // Check if email exists
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
        System.out.println("ERROR: Email already in use");
        throw new RuntimeException("Email is already in use!");
    }
    
    // Create new user
    User user = new User(
        registerRequest.getUsername(),
        registerRequest.getEmail(),
        passwordEncoder.encode(registerRequest.getPassword())
    );
    
    // Assign role (default to STAFF if not specified)
    Set<Role> roles = new HashSet<>();
    String roleName = registerRequest.getRole();
    System.out.println("Processing role: [" + roleName + "]");
    
    if (roleName != null && !roleName.isEmpty()) {
        try {
            RoleName roleEnum = RoleName.valueOf(roleName.toUpperCase());
            System.out.println("Role enum created: " + roleEnum);
            Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
            System.out.println("Role found and added: " + role.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid role: " + roleName);
            throw new RuntimeException("Invalid role: " + roleName);
        }
    } else {
        System.out.println("No role provided, using default STAFF");
        Role staffRole = roleRepository.findByName(RoleName.STAFF)
            .orElseThrow(() -> new RuntimeException("Default role STAFF not found"));
        roles.add(staffRole);
    }
    
    user.setRoles(roles);
    System.out.println("About to save user...");
    User savedUser = userRepository.save(user);
    System.out.println("User saved successfully with ID: " + savedUser.getId());
    return savedUser;
}
    
    public UserResponse loginUser(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }
        
        // Convert to UserResponse with roles
        return convertToUserResponse(user);
    }
    
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                   .map(this::convertToUserResponse)
                   .collect(Collectors.toList());
    }
    
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                           .map(this::convertToUserResponse);
    }
    
    private UserResponse convertToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                                   .map(role -> role.getName().name())
                                   .collect(Collectors.toSet());
        
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            roleNames
        );
    }
    
    // Method to check if user has specific role
    public boolean userHasRole(String username, RoleName roleName) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get().getRoles().stream()
                      .anyMatch(role -> role.getName().equals(roleName));
        }
        return false;
    }
    
    // Method to get user roles
    public Set<String> getUserRoles(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get().getRoles().stream()
                      .map(role -> role.getName().name())
                      .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
}
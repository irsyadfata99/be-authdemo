package com.example.authdemo.config;

import com.example.authdemo.entity.Role;
import com.example.authdemo.entity.RoleName;
import com.example.authdemo.entity.User;
import com.example.authdemo.repository.RoleRepository;
import com.example.authdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        initializeRoles();
        
        // Create default admin user if no users exist
        createDefaultAdmin();
    }
    
    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role(roleName);
                roleRepository.save(role);
                System.out.println("Created role: " + roleName);
            }
        }
    }
    
    private void createDefaultAdmin() {
        if (userRepository.count() == 0) {
            User admin = new User("admin", "admin@example.com", passwordEncoder.encode("admin123"));
            
            // Assign ADMIN role
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
            System.out.println("Created default admin user: admin/admin123");
        }
    }
}
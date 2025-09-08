package com.example.authdemo.entity;

public enum RoleName {
    ADMIN,    // Full access to everything
    MANAGER,  // Manage inventory, suppliers, view reports
    STAFF,    // Basic inventory operations (add stock, view items)
    VIEWER    // Read-only access
}
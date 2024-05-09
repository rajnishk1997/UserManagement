package com.optum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optum.service.PermissionService;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/initialize")
    public ResponseEntity<String> initializePermissions() {
        try {
            permissionService.initializePermissions();
            return ResponseEntity.ok("Permissions initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to initialize permissions: " + e.getMessage());
        }
    }
}


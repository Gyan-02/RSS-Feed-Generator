package com.rssfeedgenerator.userservice.controller;

import com.rssfeedgenerator.userservice.dto.AdminUserResponse;
import com.rssfeedgenerator.userservice.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final UserService userService;
    private final String adminApiKey;

    public AdminController(
            UserService userService,
            @Value("${ADMIN_API_KEY:}") String adminApiKey) {
        this.userService = userService;
        this.adminApiKey = adminApiKey;
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getRegisteredUsers(
            @RequestHeader(name = "X-Admin-Key", required = false) String suppliedKey) {
        if (adminApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Admin access is not configured.");
        }
        if (suppliedKey == null || !MessageDigest.isEqual(
                adminApiKey.getBytes(StandardCharsets.UTF_8),
                suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AdminUserResponse> users = userService.getAdminUsers();
        return ResponseEntity.ok(users);
    }
}

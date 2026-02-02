package com.ironman.controller;

import com.ironman.dto.request.LoginRequest;
import com.ironman.dto.request.RegisterRequest;
import com.ironman.dto.response.ApiResponse;
import com.ironman.dto.response.AuthResponse;
import com.ironman.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for phone: {}", request.getPhone());

        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for phone: {}", request.getPhone());

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse)
        );
    }

    @GetMapping("/test-protected")
    public ResponseEntity<ApiResponse<String>> testProtected() {
        return ResponseEntity.ok(
                ApiResponse.success("You are authenticated!", "This is a protected endpoint")
        );
    }
}
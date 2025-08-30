package com.nirmaan.controller;

import com.nirmaan.dto.ApiResponse;
import com.nirmaan.dto.LoginRequest;
import com.nirmaan.dto.LoginResponse;
import com.nirmaan.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "200", description = "Login successful"),
        @SwaggerResponse(responseCode = "401", description = "Invalid credentials"),
        @SwaggerResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", loginResponse));
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            throw e;
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (client-side token removal)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Logout successful"));
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Validate JWT token", description = "Validate if the provided JWT token is valid")
    public ResponseEntity<ApiResponse<Void>> validateToken() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Token is valid"));
    }
}
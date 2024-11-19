package com.freighthub.auth_service.controller;

import com.freighthub.auth_service.dto.ChangePwDto;
import com.freighthub.auth_service.dto.CoreBackendResponseDto;
import com.freighthub.auth_service.dto.LoginRequest;
import com.freighthub.auth_service.dto.RegisterRequest;
import com.freighthub.auth_service.entity.User;
import com.freighthub.auth_service.enums.UserRole;
import com.freighthub.auth_service.service.UserService;
import com.freighthub.auth_service.util.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.registerUser(registerRequest);
            ApiResponse<User> response = new ApiResponse<>(HttpStatus.OK.value(), "User registered successfully", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/change_pw")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePwDto changePwDto) {
        try {
            User user = userService.changePassword(changePwDto);
            ApiResponse<User> response = new ApiResponse<>(HttpStatus.OK.value(), "Password changed successfully", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<User> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            User user = userService.loginUser(loginRequest);
            String token = userService.generateJwtToken(user);

            logger.info("User: {}", user.getRole());

            CoreBackendResponseDto coreResponse = null;
            if (user.getRole() == UserRole.consigner || user.getRole() == UserRole.fleet_owner || user.getRole() == UserRole.driver) {
                 coreResponse = userService.forwardUserToCoreBackendLogin(user.getRole(), user.getId());
            }

            logger.info("Completion: {}", coreResponse);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("role", user.getRole());
            responseData.put("id", user.getId());
            if (coreResponse != null) {
                responseData.put("completion", coreResponse.getCompletion());
                responseData.put("verifyStatus", coreResponse.getVerifyStatus());
            }

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(HttpStatus.OK.value(), "Login successful", responseData);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

}

package com.freighthub.auth_service.service;
import com.freighthub.auth_service.dto.ChangePwDto;
import com.freighthub.auth_service.dto.CoreBackendResponseDto;
import com.freighthub.auth_service.enums.UserRole;

import com.freighthub.auth_service.dto.LoginRequest;
import com.freighthub.auth_service.dto.RegisterRequest;
import com.freighthub.auth_service.entity.User;
import com.freighthub.auth_service.repository.UserRepository;
import com.freighthub.auth_service.util.ApiResponse;
import com.freighthub.auth_service.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${core.backend.url}")
    private String CORE_BACKEND;

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        logger.info("Registering user: {}", registerRequest.getUsername());
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        User savedUser = userRepository.save(user);

        System.out.println(registerRequest.getFName());

        if (registerRequest.getFName() != null) {
            savedUser.setFName(registerRequest.getFName());
            user.setFName(registerRequest.getFName());
        }

        if (registerRequest.getLName() != null) {
            savedUser.setLName(registerRequest.getLName());
        }

        System.out.println(savedUser.getFName());
        System.out.println(user.getFName());

        // Forward the user to the core backend
        forwardUserToCoreBackend(savedUser);

        return savedUser;
    }


    private void forwardUserToCoreBackend(User user) {
        try {
            String coreBackendUrl = CORE_BACKEND + "/register";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<User> entity = new HttpEntity<>(user, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    coreBackendUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully forwarded user to core backend.");
            } else {
                logger.error("Failed to forward user to core backend. Status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error forwarding user to core backend: {}", e.getMessage());
        }
    }

    public CoreBackendResponseDto forwardUserToCoreBackendLogin(UserRole role, int id) {
        try {
            String coreBackendUrl = String.format("http://localhost:8081/api/login?role=%s&id=%d", role, id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<CoreBackendResponseDto> response = restTemplate.exchange(
                    coreBackendUrl, HttpMethod.POST, entity, CoreBackendResponseDto.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully forwarded user to core backend.");
                return response.getBody();
            } else {
                logger.error("Failed to forward user to core backend. Status code: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error forwarding user to core backend: {}", e.getMessage());
            return null;
        }
    }




    public User loginUser(LoginRequest loginRequest) {
        logger.info("Logging in user: {}", loginRequest.getUsername());
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return user;
        }
        throw new RuntimeException("Invalid credentials");
    }

    public String generateJwtToken(User user) {
        return jwtUtils.generateJwtToken(user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        logger.info("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User changePassword(@Valid ChangePwDto changePwDto) {
        User user = userRepository.findById(Long.valueOf(changePwDto.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(changePwDto.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePwDto.getNewPassword()));
            return userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid password");
        }
    }
}

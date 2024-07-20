package com.freighthub.auth_service.service;

import com.freighthub.auth_service.dto.LoginRequest;
import com.freighthub.auth_service.dto.RegisterRequest;
import com.freighthub.auth_service.entity.User;
import com.freighthub.auth_service.repository.UserRepository;
import com.freighthub.auth_service.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public User registerUser(RegisterRequest registerRequest) {
        logger.info("Registering user: {}", registerRequest.getUsername());
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        return userRepository.save(user);
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
}

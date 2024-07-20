package com.freighthub.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @GetMapping
    public String getProtectedData(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return "Protected data for user: " + userDetails.getUsername();
        } catch (NullPointerException e) {
            return "Protected data";
        }
    }
}

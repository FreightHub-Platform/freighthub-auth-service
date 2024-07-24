package com.freighthub.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import com.freighthub.auth_service.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private UserRole role;
}

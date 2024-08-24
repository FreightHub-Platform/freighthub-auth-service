package com.freighthub.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import com.freighthub.auth_service.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RegisterRequest implements Serializable {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private UserRole role;

    private String fName;

    private String lName;
}

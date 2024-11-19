package com.freighthub.auth_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ChangePwDto implements Serializable {
    Integer id;
    private String oldPassword;
    private String newPassword;

}

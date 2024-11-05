// CoreBackendResponse.java
package com.freighthub.auth_service.dto;

import com.freighthub.auth_service.enums.VerifyStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoreBackendResponseDto {
    private Integer completion;
    private VerifyStatus verifyStatus;
}

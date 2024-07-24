package com.freighthub.auth_service.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    admin,
    consigner,
    review_board,
    driver,
    fleet_owner
}
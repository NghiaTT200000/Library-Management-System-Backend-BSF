package com.library_management.library_management_artifact.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiMessage {
    
    REGISTER_SUCCESS("User registered successfully", true),
    LOGIN_SUCCESS("Login successful", true),
    EMAIL_ALREADY_EXISTS("email is already in use", false),
    INVALID_CREDENTIALS("Invalid email or password", false),
    
    NOT_FOUND("Resource not found", false),
    FORBIDDEN("You do not have permission to perform this action", false),
    BAD_REQUEST("Bad request", false),
    INTERNAL_ERROR("Something went wrong, please try again", false);

    private final String message;
    private final boolean ok;
}

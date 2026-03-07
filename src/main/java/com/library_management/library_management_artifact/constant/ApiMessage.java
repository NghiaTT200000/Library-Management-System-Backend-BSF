package com.library_management.library_management_artifact.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiMessage {
    REGISTER_PENDING_VERIFICATION("Registration successful. Please check your email to verify your account.",true,  HttpStatus.CREATED),
    LOGIN_SUCCESS("Login successful",true,  HttpStatus.OK),
    EMAIL_ALREADY_EXISTS("Email is already in use",false, HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("Invalid email or password",false, HttpStatus.UNAUTHORIZED),

    EMAIL_VERIFIED( "Email verified successfully. You can now log in.",true,  HttpStatus.OK),
    INVALID_VERIFICATION_TOKEN( "Invalid or expired verification token.",false, HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("Please verify your email before logging in.",false, HttpStatus.FORBIDDEN),
    EMAIL_ALREADY_VERIFIED("This email is already verified.", false, HttpStatus.BAD_REQUEST),
    VERIFICATION_EMAIL_RESENT("Verification email resent. Please check your inbox.",true,  HttpStatus.OK),

    TOKEN_REFRESHED("Token refreshed successfully",true,  HttpStatus.OK),
    LOGOUT_SUCCESS("Logged out successfully",true,  HttpStatus.OK),
    INVALID_REFRESH_TOKEN("Invalid or expired refresh token",      false, HttpStatus.UNAUTHORIZED),

    USER_CREATED      ("User created successfully",        true,  HttpStatus.CREATED),
    USER_FETCHED      ("User fetched successfully",        true,  HttpStatus.OK),
    USERS_FETCHED     ("Users fetched successfully",       true,  HttpStatus.OK),
    USER_DEACTIVATED  ("User deactivated successfully",    true,  HttpStatus.OK),
    PASSWORD_CHANGED  ("Password changed successfully",    true,  HttpStatus.OK),

    ACCOUNT_DISABLED        ("Your account has been disabled. Please contact support.", false, HttpStatus.FORBIDDEN),
    INVALID_CURRENT_PASSWORD("Current password is incorrect",                           false, HttpStatus.BAD_REQUEST),

    BOOK_CREATED    ("Book created successfully",    true,  HttpStatus.CREATED),
    BOOK_FETCHED    ("Book fetched successfully",    true,  HttpStatus.OK),
    BOOKS_FETCHED   ("Books fetched successfully",   true,  HttpStatus.OK),
    BOOK_UPDATED    ("Book updated successfully",    true,  HttpStatus.OK),
    BOOK_DELETED    ("Book deleted successfully",    true,  HttpStatus.OK),

    CATEGORY_FETCHED    ("Category fetched successfully",    true,  HttpStatus.OK),
    CATEGORIES_FETCHED  ("Categories fetched successfully",  true,  HttpStatus.OK),

    NOT_FOUND("Resource not found",false, HttpStatus.NOT_FOUND),
    FORBIDDEN("You do not have permission to do this",false, HttpStatus.FORBIDDEN),
    BAD_REQUEST("Bad request",false, HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("Something went wrong, please try again later", false, HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final boolean ok;
    private final HttpStatus status;
}
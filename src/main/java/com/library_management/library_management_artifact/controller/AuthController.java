package com.library_management.library_management_artifact.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.ChangePasswordRequest;
import com.library_management.library_management_artifact.dto.request.LoginRequest;
import com.library_management.library_management_artifact.dto.request.RegisterRequest;
import com.library_management.library_management_artifact.dto.request.ResendVerificationRequest;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.AuthResponse;
import com.library_management.library_management_artifact.dto.response.RegisterResponse;
import com.library_management.library_management_artifact.dto.response.UserResponse;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.exception.InvalidRefreshTokenException;
import com.library_management.library_management_artifact.service.AuthService;
import com.library_management.library_management_artifact.util.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse data = authService.register(request);
        return ResponseEntity
                .status(ApiMessage.REGISTER_PENDING_VERIFICATION.getStatus())
                .body(ApiResponse.success(ApiMessage.REGISTER_PENDING_VERIFICATION.getMessage(), data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse data = authService.login(request);
        cookieUtils.addRefreshTokenCookie(response, data.getRefreshToken());
        return ResponseEntity
                .status(ApiMessage.LOGIN_SUCCESS.getStatus())
                .body(ApiResponse.success(ApiMessage.LOGIN_SUCCESS.getMessage(), data));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity
                .status(ApiMessage.EMAIL_VERIFIED.getStatus())
                .body(ApiResponse.success(ApiMessage.EMAIL_VERIFIED.getMessage()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        authService.resendVerificationEmail(request);
        return ResponseEntity
                .status(ApiMessage.VERIFICATION_EMAIL_RESENT.getStatus())
                .body(ApiResponse.success(ApiMessage.VERIFICATION_EMAIL_RESENT.getMessage()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String rawToken = cookieUtils.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        ApiMessage.INVALID_REFRESH_TOKEN.getMessage()));

        AuthResponse data = authService.refresh(rawToken);
        cookieUtils.addRefreshTokenCookie(response, data.getRefreshToken());
        return ResponseEntity
                .status(ApiMessage.TOKEN_REFRESHED.getStatus())
                .body(ApiResponse.success(ApiMessage.TOKEN_REFRESHED.getMessage(), data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        cookieUtils.getRefreshTokenFromCookie(request)
                .ifPresent(authService::logout);
        cookieUtils.clearRefreshTokenCookie(response);
        return ResponseEntity
                .status(ApiMessage.LOGOUT_SUCCESS.getStatus())
                .body(ApiResponse.success(ApiMessage.LOGOUT_SUCCESS.getMessage()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal User user
    ) {
        UserResponse data = authService.me(user);
        return ResponseEntity
                .status(ApiMessage.USER_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.USER_FETCHED.getMessage(), data));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(user, request);
        return ResponseEntity
                .status(ApiMessage.PASSWORD_CHANGED.getStatus())
                .body(ApiResponse.success(ApiMessage.PASSWORD_CHANGED.getMessage()));
    }
}

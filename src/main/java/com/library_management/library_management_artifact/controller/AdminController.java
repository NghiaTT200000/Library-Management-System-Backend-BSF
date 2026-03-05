package com.library_management.library_management_artifact.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.CreateUserRequest;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.UserResponse;
import com.library_management.library_management_artifact.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse data = authService.createUser(request);
        return ResponseEntity
                .status(ApiMessage.USER_CREATED.getStatus())
                .body(ApiResponse.success(ApiMessage.USER_CREATED.getMessage(), data));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> data = authService.getAllUsers();
        return ResponseEntity
                .status(ApiMessage.USERS_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.USERS_FETCHED.getMessage(), data));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        authService.deactivateUser(id);
        return ResponseEntity
                .status(ApiMessage.USER_DEACTIVATED.getStatus())
                .body(ApiResponse.success(ApiMessage.USER_DEACTIVATED.getMessage()));
    }
}

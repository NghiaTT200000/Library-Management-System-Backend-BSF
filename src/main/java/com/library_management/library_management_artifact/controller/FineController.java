package com.library_management.library_management_artifact.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.FineDetailResponse;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.service.FineService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fines", description = "Fine management endpoints")
public class FineController {

    private final FineService fineService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<FineDetailResponse>>> getAll(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String bookTitle,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity
                .status(ApiMessage.FINES_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.FINES_FETCHED.getMessage(),
                        fineService.getAll(userEmail, itemCode, bookTitle, pageable)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<FineDetailResponse>>> getMy(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String bookTitle,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity
                .status(ApiMessage.FINES_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.FINES_FETCHED.getMessage(),
                        fineService.getMyFines(currentUser, itemCode, bookTitle, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FineDetailResponse>> getById(@PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity
                .status(ApiMessage.FINE_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.FINE_FETCHED.getMessage(),
                        fineService.getByIdForUser(id, currentUser)));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FineDetailResponse>> pay(@PathVariable UUID id) {
        return ResponseEntity
                .status(ApiMessage.FINE_PAID.getStatus())
                .body(ApiResponse.success(ApiMessage.FINE_PAID.getMessage(),
                        fineService.pay(id)));
    }
}

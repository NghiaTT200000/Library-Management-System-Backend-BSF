package com.library_management.library_management_artifact.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.BorrowRequest;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.BorrowRecordDetailResponse;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.service.BorrowService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Borrows", description = "Borrow and return management endpoints")
public class BorrowController {

    private final BorrowService borrowService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BorrowRecordDetailResponse>>> getAll(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity
                .status(ApiMessage.BORROWS_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BORROWS_FETCHED.getMessage(),
                        borrowService.getAll(currentUser, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BorrowRecordDetailResponse>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity
                .status(ApiMessage.BORROW_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BORROW_FETCHED.getMessage(),
                        borrowService.getById(id, currentUser)));
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BorrowRecordDetailResponse>>> getByItemId(
            @PathVariable UUID itemId) {
        return ResponseEntity
                .status(ApiMessage.BORROWS_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BORROWS_FETCHED.getMessage(),
                        borrowService.getByItemId(itemId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowRecordDetailResponse>> borrow(
            @Valid @RequestBody BorrowRequest request) {
        return ResponseEntity
                .status(ApiMessage.BORROW_CREATED.getStatus())
                .body(ApiResponse.success(ApiMessage.BORROW_CREATED.getMessage(),
                        borrowService.borrow(request)));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowRecordDetailResponse>> returnBook(
            @PathVariable UUID id) {
        return ResponseEntity
                .status(ApiMessage.BORROW_RETURNED.getStatus())
                .body(ApiResponse.success(ApiMessage.BORROW_RETURNED.getMessage(),
                        borrowService.returnBook(id)));
    }
}

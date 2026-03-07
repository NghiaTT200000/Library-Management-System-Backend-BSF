package com.library_management.library_management_artifact.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.BookItemRequest;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.BookItemResponse;
import com.library_management.library_management_artifact.service.BookItemService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/book-items")
@RequiredArgsConstructor
@Tag(name = "Book Items", description = "Physical copy management endpoints")
public class BookItemController {

    private final BookItemService bookItemService;

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<List<BookItemResponse>>> getByBookId(@PathVariable UUID bookId) {
        return ResponseEntity
                .status(ApiMessage.BOOK_ITEMS_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_ITEMS_FETCHED.getMessage(),
                        bookItemService.getByBookId(bookId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookItemResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity
                .status(ApiMessage.BOOK_ITEM_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_ITEM_FETCHED.getMessage(),
                        bookItemService.getById(id)));
    }

    @GetMapping("/code/{itemCode}")
    public ResponseEntity<ApiResponse<BookItemResponse>> getByItemCode(@PathVariable String itemCode) {
        return ResponseEntity
                .status(ApiMessage.BOOK_ITEM_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_ITEM_FETCHED.getMessage(),
                        bookItemService.getByItemCode(itemCode)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<BookItemResponse>> create(@Valid @RequestBody BookItemRequest request) {
        return ResponseEntity
                .status(ApiMessage.BOOK_ITEM_CREATED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_ITEM_CREATED.getMessage(),
                        bookItemService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<BookItemResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody BookItemRequest request) {
        return ResponseEntity
                .status(ApiMessage.BOOK_ITEM_UPDATED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_ITEM_UPDATED.getMessage(),
                        bookItemService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        bookItemService.delete(id);
        return ResponseEntity
                .status(ApiMessage.BOOK_ITEM_DELETED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_ITEM_DELETED.getMessage()));
    }
}

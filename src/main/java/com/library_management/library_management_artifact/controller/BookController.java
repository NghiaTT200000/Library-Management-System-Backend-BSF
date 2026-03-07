package com.library_management.library_management_artifact.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.BookRequest;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.BookDetailResponse;
import com.library_management.library_management_artifact.dto.response.BookResponse;
import com.library_management.library_management_artifact.service.BookService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management endpoints")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return ResponseEntity
                .status(ApiMessage.BOOKS_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOKS_FETCHED.getMessage(),
                        bookService.getAll(search, author, category, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity
                .status(ApiMessage.BOOK_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_FETCHED.getMessage(), bookService.getById(id)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(content = @Content(encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<BookDetailResponse>> create(
            @RequestPart("data") @Valid BookRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity
                .status(ApiMessage.BOOK_CREATED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_CREATED.getMessage(), bookService.create(request, file)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @RequestBody(content = @Content(encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<BookDetailResponse>> update(
            @PathVariable UUID id,
            @RequestPart("data") @Valid BookRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity
                .status(ApiMessage.BOOK_UPDATED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_UPDATED.getMessage(), bookService.update(id, request, file)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        bookService.delete(id);
        return ResponseEntity
                .status(ApiMessage.BOOK_DELETED.getStatus())
                .body(ApiResponse.success(ApiMessage.BOOK_DELETED.getMessage()));
    }
}

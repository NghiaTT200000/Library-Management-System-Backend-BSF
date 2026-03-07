package com.library_management.library_management_artifact.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.CategoryResponse;
import com.library_management.library_management_artifact.service.CategoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category read endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity
                .status(ApiMessage.CATEGORIES_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.CATEGORIES_FETCHED.getMessage(), categoryService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity
                .status(ApiMessage.CATEGORY_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.CATEGORY_FETCHED.getMessage(), categoryService.getById(id)));
    }
}

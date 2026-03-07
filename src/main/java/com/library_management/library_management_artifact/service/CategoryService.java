package com.library_management.library_management_artifact.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.library_management.library_management_artifact.dto.response.CategoryResponse;
import com.library_management.library_management_artifact.entity.Category;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.CategoryMapper;
import com.library_management.library_management_artifact.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAll() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    public CategoryResponse getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return categoryMapper.toResponse(category);
    }
}

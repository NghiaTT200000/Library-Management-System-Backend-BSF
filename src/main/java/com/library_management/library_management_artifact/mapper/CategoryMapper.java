package com.library_management.library_management_artifact.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.library_management.library_management_artifact.dto.response.CategoryResponse;
import com.library_management.library_management_artifact.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);
}

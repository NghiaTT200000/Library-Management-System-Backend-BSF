package com.library_management.library_management_artifact.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.library_management.library_management_artifact.dto.request.RegisterRequest;
import com.library_management.library_management_artifact.dto.response.UserResponse;
import com.library_management.library_management_artifact.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role",     ignore = true)
    @Mapping(target = "active",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);
}

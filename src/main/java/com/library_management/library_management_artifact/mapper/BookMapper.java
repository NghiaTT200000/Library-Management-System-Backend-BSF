package com.library_management.library_management_artifact.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.library_management.library_management_artifact.dto.request.BookRequest;
import com.library_management.library_management_artifact.dto.response.BookResponse;
import com.library_management.library_management_artifact.entity.Book;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "availableCopies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "descriptionEmbedding", ignore = true)
    @Mapping(target = "aiSummary", ignore = true)
    Book toEntity(BookRequest request);

    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "availableCopies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "descriptionEmbedding", ignore = true)
    @Mapping(target = "aiSummary", ignore = true)
    void updateEntity(BookRequest request, @MappingTarget Book book);
}

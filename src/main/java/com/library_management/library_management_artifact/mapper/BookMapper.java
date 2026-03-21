package com.library_management.library_management_artifact.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.library_management.library_management_artifact.dto.request.BookRequest;
import com.library_management.library_management_artifact.dto.response.BookResponse;
import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.repository.BookItemRepository;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public abstract class BookMapper {

    @Autowired
    protected BookItemRepository bookItemRepository;

    @Mapping(target = "categories",           ignore = true)
    @Mapping(target = "items",                ignore = true)
    @Mapping(target = "active",               ignore = true)
    @Mapping(target = "createdAt",            ignore = true)
    @Mapping(target = "updatedAt",            ignore = true)
    @Mapping(target = "coverImageUrl",        ignore = true)
    @Mapping(target = "descriptionEmbedding", ignore = true)
    @Mapping(target = "aiSummary",            ignore = true)
    public abstract Book toEntity(BookRequest request);

    @Mapping(target = "totalCopies",     ignore = true)
    @Mapping(target = "availableCopies", ignore = true)
    public abstract BookResponse toResponse(Book book);

    @AfterMapping
    protected void computeCounts(Book book, @MappingTarget BookResponse.BookResponseBuilder builder) {
        builder.totalCopies((int) bookItemRepository.countByBookIsbn(book.getIsbn()));
        builder.availableCopies((int) bookItemRepository.countByBookIsbnAndStatus(book.getIsbn(), BookItemStatus.AVAILABLE));
    }

    @Mapping(target = "isbn",                 ignore = true)
    @Mapping(target = "categories",           ignore = true)
    @Mapping(target = "active",               ignore = true)
    @Mapping(target = "items",                ignore = true)
    @Mapping(target = "createdAt",            ignore = true)
    @Mapping(target = "updatedAt",            ignore = true)
    @Mapping(target = "coverImageUrl",        ignore = true)
    @Mapping(target = "descriptionEmbedding", ignore = true)
    @Mapping(target = "aiSummary",            ignore = true)
    public abstract void updateEntity(BookRequest request, @MappingTarget Book book);
}

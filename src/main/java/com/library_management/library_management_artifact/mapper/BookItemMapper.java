package com.library_management.library_management_artifact.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.library_management.library_management_artifact.dto.response.BookItemResponse;
import com.library_management.library_management_artifact.entity.BookItem;

@Mapper(componentModel = "spring")
public interface BookItemMapper {
    @Mapping(source = "book.isbn", target = "bookIsbn")
    BookItemResponse toResponse(BookItem bookItem);

    @Mapping(source = "book.isbn", target = "bookIsbn")
    List<BookItemResponse> toResponseList(List<BookItem> items);
}

package com.library_management.library_management_artifact.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.library_management.library_management_artifact.dto.response.BookItemResponse;
import com.library_management.library_management_artifact.entity.BookItem;

@Mapper(componentModel = "spring")
public interface BookItemMapper {
    BookItemResponse toResponse(BookItem bookItem);
    List<BookItemResponse> toResponseList(List<BookItem> items);
}

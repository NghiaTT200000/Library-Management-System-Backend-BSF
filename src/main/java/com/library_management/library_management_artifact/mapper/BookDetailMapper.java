package com.library_management.library_management_artifact.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.library_management.library_management_artifact.dto.response.BookDetailResponse;
import com.library_management.library_management_artifact.dto.response.BookItemResponse;
import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.BookItemStatus;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BookItemMapper.class})
public interface BookDetailMapper {

    @Mapping(target = "totalCopies",     ignore = true)
    @Mapping(target = "availableCopies", ignore = true)
    BookDetailResponse toDetailResponse(Book book);

    @AfterMapping
    default void computeCopyCounts(@MappingTarget BookDetailResponse response) {
        if (response.getItems() == null) return;
        response.setTotalCopies(response.getItems().size());
        response.setAvailableCopies((int) response.getItems().stream()
                .filter(i -> i.getStatus() == BookItemStatus.AVAILABLE)
                .count());
    }
}

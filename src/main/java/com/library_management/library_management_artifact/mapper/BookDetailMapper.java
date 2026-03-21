package com.library_management.library_management_artifact.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.library_management.library_management_artifact.dto.response.BookDetailResponse;
import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.repository.BookItemRepository;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BookItemMapper.class})
public abstract class BookDetailMapper {

    @Autowired
    protected BookItemRepository bookItemRepository;

    @Mapping(target = "totalCopies",     ignore = true)
    @Mapping(target = "availableCopies", ignore = true)
    public abstract BookDetailResponse toDetailResponse(Book book);

    @AfterMapping
    protected void computeCopyCounts(Book book, @MappingTarget BookDetailResponse.BookDetailResponseBuilder builder) {
        builder.totalCopies((int) bookItemRepository.countByBookIsbn(book.getIsbn()));
        builder.availableCopies((int) bookItemRepository.countByBookIsbnAndStatus(book.getIsbn(), BookItemStatus.AVAILABLE));
    }
}

package com.library_management.library_management_artifact.mapper;

import org.mapstruct.Mapper;

import com.library_management.library_management_artifact.dto.response.BookItemDetailResponse;
import com.library_management.library_management_artifact.entity.BookItem;

@Mapper(componentModel = "spring", uses = {BookMapper.class})
public interface BookItemDetailMapper {
    BookItemDetailResponse toDetailResponse(BookItem bookItem);
}

package com.library_management.library_management_artifact.mapper;

import org.mapstruct.Mapper;

import com.library_management.library_management_artifact.dto.response.BorrowRecordDetailResponse;
import com.library_management.library_management_artifact.entity.BorrowRecord;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BookItemDetailMapper.class})
public interface BorrowRecordDetailMapper {
    BorrowRecordDetailResponse toDetailResponse(BorrowRecord borrowRecord);
}

package com.library_management.library_management_artifact.mapper;

import org.mapstruct.Mapper;

import com.library_management.library_management_artifact.dto.response.FineDetailResponse;
import com.library_management.library_management_artifact.entity.Fine;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BorrowRecordDetailMapper.class})
public interface FineDetailMapper {
    FineDetailResponse toDetailResponse(Fine fine);
}

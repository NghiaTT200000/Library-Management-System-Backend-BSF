package com.library_management.library_management_artifact.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.library_management.library_management_artifact.dto.response.BorrowRecordDetailResponse;
import com.library_management.library_management_artifact.dto.response.FineSummaryResponse;
import com.library_management.library_management_artifact.entity.BorrowRecord;
import com.library_management.library_management_artifact.entity.Fine;
import com.library_management.library_management_artifact.repository.FineRepository;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BookItemDetailMapper.class})
public abstract class BorrowRecordDetailMapper {

    @Autowired
    protected FineRepository fineRepository;

    public abstract BorrowRecordDetailResponse toDetailResponse(BorrowRecord borrowRecord);

    @AfterMapping
    protected void attachFine(BorrowRecord borrowRecord,
                              @MappingTarget BorrowRecordDetailResponse.BorrowRecordDetailResponseBuilder builder) {
        fineRepository.findByBorrowRecordId(borrowRecord.getId()).ifPresent(fine ->
            builder.fine(toFineSummary(fine))
        );
    }

    private FineSummaryResponse toFineSummary(Fine fine) {
        return FineSummaryResponse.builder()
                .id(fine.getId())
                .daysOverdue(fine.getDaysOverdue())
                .amount(fine.getAmount())
                .status(fine.getStatus())
                .createdAt(fine.getCreatedAt())
                .build();
    }
}

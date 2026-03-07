package com.library_management.library_management_artifact.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.library_management.library_management_artifact.entity.FineStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineDetailResponse {
    private UUID id;
    private UserResponse user;
    private BorrowRecordDetailResponse borrowRecord;
    private Integer daysOverdue;
    private BigDecimal amount;
    private FineStatus status;
    private LocalDateTime createdAt;
}

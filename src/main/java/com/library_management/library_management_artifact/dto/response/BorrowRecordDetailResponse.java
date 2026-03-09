package com.library_management.library_management_artifact.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.library_management.library_management_artifact.entity.BorrowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordDetailResponse {
    private UUID id;
    private UserResponse user;
    private BookItemDetailResponse bookItem;
    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;
    private BorrowStatus status;
    private LocalDateTime createdAt;
    private FineSummaryResponse fine;
}

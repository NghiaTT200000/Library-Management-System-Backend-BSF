package com.library_management.library_management_artifact.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.entity.ItemCondition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookItemResponse {
    private UUID id;
    private String itemCode;
    private String locationCode;
    private String description;
    private LocalDate acquiredAt;
    private ItemCondition condition;
    private BookItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

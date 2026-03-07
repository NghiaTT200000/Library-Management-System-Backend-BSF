package com.library_management.library_management_artifact.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.entity.ItemCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookItemRequest {

    @NotNull
    private UUID bookId;

    @NotBlank
    private String itemCode;

    private String locationCode;
    private String description;
    private LocalDate acquiredAt;
    private ItemCondition condition;
    private BookItemStatus status;
}

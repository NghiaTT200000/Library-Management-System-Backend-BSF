package com.library_management.library_management_artifact.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotNull
    private UUID bookItemId;

    @NotNull
    private UUID userId;
}

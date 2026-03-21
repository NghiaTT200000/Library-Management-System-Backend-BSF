package com.library_management.library_management_artifact.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookItemRequest {

    @NotBlank
    private String bookIsbn;

    @NotBlank
    private String itemCode;

    private String locationCode;
    private String description;
    private LocalDate acquiredAt;
}

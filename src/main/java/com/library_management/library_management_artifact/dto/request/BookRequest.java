package com.library_management.library_management_artifact.dto.request;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    private String isbn;
    private String publisher;
    private Integer publishedYear;
    private String description;
    private String coverImageUrl;

    @Min(1)
    private Integer totalCopies = 1;

    private Set<String> categoryNames = new HashSet<>();
}

package com.library_management.library_management_artifact.dto.request;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank
    private String isbn;

    @NotBlank
    private String title;

    @NotBlank
    private String author;
    private String publisher;
    private Integer publishedYear;
    private String description;

    private Set<String> categoryNames = new HashSet<>();
}

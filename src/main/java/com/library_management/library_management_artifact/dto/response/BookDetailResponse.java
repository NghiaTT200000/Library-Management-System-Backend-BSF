package com.library_management.library_management_artifact.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailResponse {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publishedYear;
    private String description;
    private String coverImageUrl;
    private Set<CategoryResponse> categories;
    private List<BookItemResponse> items;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

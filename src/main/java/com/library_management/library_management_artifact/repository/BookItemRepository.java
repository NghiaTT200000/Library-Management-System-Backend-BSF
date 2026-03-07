package com.library_management.library_management_artifact.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.BookItem;
import com.library_management.library_management_artifact.entity.BookItemStatus;

public interface BookItemRepository extends JpaRepository<BookItem, UUID> {
    Optional<BookItem> findByItemCode(String itemCode);
    List<BookItem> findByBookId(UUID bookId);
    List<BookItem> findByBookIdAndStatus(UUID bookId, BookItemStatus status);
    boolean existsByItemCode(String itemCode);
    long countByBookId(UUID bookId);
    long countByBookIdAndStatus(UUID bookId, BookItemStatus status);
}

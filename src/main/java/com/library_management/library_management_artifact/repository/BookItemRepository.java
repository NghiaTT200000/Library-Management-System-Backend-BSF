package com.library_management.library_management_artifact.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.library_management.library_management_artifact.entity.BookItem;
import com.library_management.library_management_artifact.entity.BookItemStatus;

public interface BookItemRepository extends JpaRepository<BookItem, UUID>, JpaSpecificationExecutor<BookItem> {
    Optional<BookItem> findByItemCode(String itemCode);
    List<BookItem> findByBookIsbn(String isbn);
    List<BookItem> findByBookIsbnAndStatus(String isbn, BookItemStatus status);
    boolean existsByItemCode(String itemCode);
    long countByBookIsbn(String isbn);
    long countByBookIsbnAndStatus(String isbn, BookItemStatus status);
}

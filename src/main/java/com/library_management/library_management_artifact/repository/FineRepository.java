package com.library_management.library_management_artifact.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library_management.library_management_artifact.entity.Fine;
import com.library_management.library_management_artifact.entity.FineStatus;

public interface FineRepository extends JpaRepository<Fine, UUID> {
    Optional<Fine> findByBorrowRecordId(UUID borrowRecordId);
    List<Fine> findAllByStatus(FineStatus status);

    @Query("SELECT f FROM Fine f WHERE " +
           "(:userEmail IS NULL OR LOWER(f.user.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND " +
           "(:itemCode IS NULL OR LOWER(f.borrowRecord.bookItem.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))) AND " +
           "(:bookTitle IS NULL OR LOWER(f.borrowRecord.bookItem.book.title) LIKE LOWER(CONCAT('%', :bookTitle, '%')))")
    Page<Fine> searchAll(@Param("userEmail") String userEmail,
                         @Param("itemCode") String itemCode,
                         @Param("bookTitle") String bookTitle,
                         Pageable pageable);

    @Query("SELECT f FROM Fine f WHERE f.user.id = :userId AND " +
           "(:itemCode IS NULL OR LOWER(f.borrowRecord.bookItem.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%'))) AND " +
           "(:bookTitle IS NULL OR LOWER(f.borrowRecord.bookItem.book.title) LIKE LOWER(CONCAT('%', :bookTitle, '%')))")
    Page<Fine> searchByUser(@Param("userId") UUID userId,
                             @Param("itemCode") String itemCode,
                             @Param("bookTitle") String bookTitle,
                             Pageable pageable);
}

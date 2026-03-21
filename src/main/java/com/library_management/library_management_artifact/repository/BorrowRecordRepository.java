package com.library_management.library_management_artifact.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library_management.library_management_artifact.entity.BorrowRecord;
import com.library_management.library_management_artifact.entity.BorrowStatus;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID> {
    List<BorrowRecord> findByBookItemId(UUID bookItemId);
    Optional<BorrowRecord> findByBookItemIdAndStatus(UUID bookItemId, BorrowStatus status);
    List<BorrowRecord> findAllByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);
    List<BorrowRecord> findAllByStatus(BorrowStatus status);
    long countByStatus(BorrowStatus status);

    @Query("SELECT b FROM BorrowRecord b WHERE " +
           "(:userEmail IS NULL OR LOWER(b.user.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND " +
           "(:itemCode IS NULL OR LOWER(b.bookItem.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%')))")
    Page<BorrowRecord> searchAll(@Param("userEmail") String userEmail,
                                 @Param("itemCode") String itemCode,
                                 Pageable pageable);

    @Query("SELECT b FROM BorrowRecord b WHERE b.user.id = :userId AND " +
           "(:itemCode IS NULL OR LOWER(b.bookItem.itemCode) LIKE LOWER(CONCAT('%', :itemCode, '%')))")
    Page<BorrowRecord> searchByUser(@Param("userId") UUID userId,
                                    @Param("itemCode") String itemCode,
                                    Pageable pageable);
}

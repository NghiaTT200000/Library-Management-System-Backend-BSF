package com.library_management.library_management_artifact.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import com.library_management.library_management_artifact.entity.BorrowRecord;
import com.library_management.library_management_artifact.entity.BorrowStatus;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID> {
    Page<BorrowRecord> findByUserId(UUID userId, Pageable pageable);
    List<BorrowRecord> findByBookItemId(UUID bookItemId);
    Optional<BorrowRecord> findByBookItemIdAndStatus(UUID bookItemId, BorrowStatus status);
    List<BorrowRecord> findAllByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);
    List<BorrowRecord> findAllByStatus(BorrowStatus status);
}

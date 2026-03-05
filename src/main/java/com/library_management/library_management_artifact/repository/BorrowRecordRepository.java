package com.library_management.library_management_artifact.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.BorrowRecord;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID> {
}

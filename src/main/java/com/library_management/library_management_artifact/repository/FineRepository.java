package com.library_management.library_management_artifact.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.Fine;

public interface FineRepository extends JpaRepository<Fine, UUID> {
    Page<Fine> findByUserId(UUID userId, Pageable pageable);
}

package com.library_management.library_management_artifact.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}

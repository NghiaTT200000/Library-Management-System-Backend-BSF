package com.library_management.library_management_artifact.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}

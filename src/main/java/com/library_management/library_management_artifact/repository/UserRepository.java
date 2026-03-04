package com.library_management.library_management_artifact.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

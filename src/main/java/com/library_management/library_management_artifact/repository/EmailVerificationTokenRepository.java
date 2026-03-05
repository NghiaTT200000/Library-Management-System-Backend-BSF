package com.library_management.library_management_artifact.repository;

import java.util.Optional;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.library_management.library_management_artifact.entity.EmailVerificationToken;
import com.library_management.library_management_artifact.entity.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(String token);
    boolean existsByUser(User user);
    void deleteByUser(User user);
}

package com.example.bookstore.repository;

import com.example.bookstore.model.MailToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailTokenRepository extends JpaRepository<MailToken, Integer> {
    Optional<MailToken> findByToken(String token);
}

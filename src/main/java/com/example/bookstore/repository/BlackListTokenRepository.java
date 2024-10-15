package com.example.bookstore.repository;

import com.example.bookstore.model.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListTokenRepository extends JpaRepository<BlackListToken, String> {
    boolean existsByToken(String token);
}

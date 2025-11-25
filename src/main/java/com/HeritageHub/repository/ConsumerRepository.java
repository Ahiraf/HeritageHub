package com.HeritageHub.repository;

import com.HeritageHub.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumerRepository extends JpaRepository<Consumer, String> {
    Optional<Consumer> findByEmailIgnoreCase(String email);
    Optional<Consumer> findByEmailIgnoreCaseAndPassword(String email, String password);
    Optional<Consumer> findByApiKey(String apiKey);
}

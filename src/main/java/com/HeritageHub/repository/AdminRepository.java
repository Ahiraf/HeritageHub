package com.HeritageHub.repository;

import com.HeritageHub.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmailIgnoreCase(String email);
    Optional<Admin> findByEmailIgnoreCaseAndPassword(String email, String password);
    Optional<Admin> findByApiKey(String apiKey);
}

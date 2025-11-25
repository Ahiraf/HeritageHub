package com.HeritageHub.repository;

import com.HeritageHub.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, String> {
    Optional<Seller> findByEmailIgnoreCase(String email);
    Optional<Seller> findByEmailIgnoreCaseAndPassword(String email, String password);
    List<Seller> findByVerified(Boolean verified);
    Optional<Seller> findByApiKey(String apiKey);
}

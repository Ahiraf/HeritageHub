package com.HeritageHub.repository;

import com.HeritageHub.model.Product;
import com.HeritageHub.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
}

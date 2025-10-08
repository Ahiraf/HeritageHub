package com.HeritageHub.repository;

import com.HeritageHub.model.Product;
import com.HeritageHub.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller(Seller seller);
    List<Product> findByCategoryIgnoreCase(String category);
}

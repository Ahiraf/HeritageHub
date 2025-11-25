package com.HeritageHub.service;

import com.HeritageHub.model.Admin;
import com.HeritageHub.model.Product;
import com.HeritageHub.model.Seller;
import com.HeritageHub.repository.AdminRepository;
import com.HeritageHub.repository.ProductRepository;
import com.HeritageHub.repository.SellerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    public ProductService(ProductRepository productRepository,
                          SellerRepository sellerRepository,
                          AdminRepository adminRepository) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.adminRepository = adminRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }

    public Product create(Product product, String sellerNid, Long approvedById) {
        Seller seller = sellerRepository.findById(sellerNid)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerNid));
        product.setSeller(seller);
        if (approvedById != null) {
            Admin admin = adminRepository.findById(approvedById)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + approvedById));
            product.setApprovedBy(admin);
        }
        if (product.getDateAdded() == null) {
            product.setDateAdded(LocalDateTime.now());
        }
        if (product.getBiddable() == null) {
            product.setBiddable(Boolean.FALSE);
        }
        Product saved = productRepository.save(product);
        return findById(saved.getId());
    }

    public Product update(Long id, Product updates, String sellerNid, Long approvedById) {
        Product existing = findById(id);
        existing.setProductName(updates.getProductName());
        existing.setCategory(updates.getCategory());
        existing.setMaterialType(updates.getMaterialType());
        existing.setSaleType(updates.getSaleType());
        existing.setCraftType(updates.getCraftType());
        existing.setColor(updates.getColor());
        existing.setProductPrice(updates.getProductPrice());
        existing.setInStock(updates.getInStock());
        existing.setBiddable(updates.getBiddable() != null ? updates.getBiddable() : existing.getBiddable());
        existing.setSize(updates.getSize());
        existing.setWeight(updates.getWeight());
        existing.setProductionTime(updates.getProductionTime());
        existing.setDescription(updates.getDescription());
        existing.setUploadImage(updates.getUploadImage());
        existing.setDateAdded(updates.getDateAdded() != null ? updates.getDateAdded() : existing.getDateAdded());
        if (sellerNid != null) {
            Seller seller = sellerRepository.findById(sellerNid)
                    .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerNid));
            existing.setSeller(seller);
        }
        if (approvedById != null) {
            Admin admin = adminRepository.findById(approvedById)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + approvedById));
            existing.setApprovedBy(admin);
        }
        productRepository.save(existing);
        return findById(existing.getId());
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }
}

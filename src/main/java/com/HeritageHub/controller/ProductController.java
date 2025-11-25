package com.HeritageHub.controller;

import com.HeritageHub.dto.ProductResponse;
import com.HeritageHub.mapper.ProductMapper;
import com.HeritageHub.model.Product;
import com.HeritageHub.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam(value = "category", required = false) String category) {
        List<Product> products = category != null && !category.isBlank()
                ? productService.findByCategory(category)
                : productService.findAll();
        return ResponseEntity.ok(products.stream().map(ProductMapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ProductMapper.toResponse(productService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody Product product,
                                                         @RequestParam("sellerNid") String sellerNid,
                                                         @RequestParam(value = "approvedById", required = false) Long approvedById) {
        return ResponseEntity.ok(ProductMapper.toResponse(productService.create(product, sellerNid, approvedById)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @RequestBody Product product,
                                                         @RequestParam(value = "sellerNid", required = false) String sellerNid,
                                                         @RequestParam(value = "approvedById", required = false) Long approvedById) {
        return ResponseEntity.ok(ProductMapper.toResponse(productService.update(id, product, sellerNid, approvedById)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

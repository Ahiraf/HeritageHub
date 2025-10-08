package com.HeritageHub.controller;

import com.HeritageHub.model.Seller;
import com.HeritageHub.service.SellerService;
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

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers() {
        return ResponseEntity.ok(sellerService.findAll());
    }

    @GetMapping("/{sellerNid}")
    public ResponseEntity<Seller> getSeller(@PathVariable String sellerNid) {
        return ResponseEntity.ok(sellerService.findById(sellerNid));
    }

    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller,
                                               @RequestParam(value = "managerId", required = false) Long managerId) {
        return ResponseEntity.ok(sellerService.create(seller, managerId));
    }

    @PutMapping("/{sellerNid}")
    public ResponseEntity<Seller> updateSeller(@PathVariable String sellerNid,
                                               @RequestBody Seller seller,
                                               @RequestParam(value = "managerId", required = false) Long managerId) {
        return ResponseEntity.ok(sellerService.update(sellerNid, seller, managerId));
    }

    @DeleteMapping("/{sellerNid}")
    public ResponseEntity<Void> deleteSeller(@PathVariable String sellerNid) {
        sellerService.delete(sellerNid);
        return ResponseEntity.noContent().build();
    }
}

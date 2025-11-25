package com.HeritageHub.controller;

import com.HeritageHub.dto.SellerResponse;
import com.HeritageHub.mapper.SellerMapper;
import com.HeritageHub.model.Seller;
import com.HeritageHub.service.SellerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SellerResponse>> getAllSellers(@RequestParam(value = "verified", required = false) Boolean verified) {
        List<Seller> sellers = verified != null ? sellerService.findByVerified(verified) : sellerService.findAll();
        return ResponseEntity.ok(sellers.stream().map(SellerMapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{sellerNid}")
    public ResponseEntity<SellerResponse> getSeller(@PathVariable String sellerNid) {
        return ResponseEntity.ok(SellerMapper.toResponse(sellerService.findById(sellerNid)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerResponse> createSeller(@RequestBody Seller seller,
                                               @RequestParam(value = "managerId", required = false) Long managerId) {
        return ResponseEntity.ok(SellerMapper.toResponse(sellerService.create(seller, managerId)));
    }

    @PutMapping("/{sellerNid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerResponse> updateSeller(@PathVariable String sellerNid,
                                               @RequestBody Seller seller,
                                               @RequestParam(value = "managerId", required = false) Long managerId) {
        return ResponseEntity.ok(SellerMapper.toResponse(sellerService.update(sellerNid, seller, managerId)));
    }

    @PostMapping("/{sellerNid}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SellerResponse> verifySeller(@PathVariable String sellerNid,
                                                       @RequestParam(value = "adminId", required = false) Long adminId,
                                                       @RequestParam(value = "verified", defaultValue = "true") boolean verified) {
        return ResponseEntity.ok(SellerMapper.toResponse(sellerService.verifySeller(sellerNid, adminId, verified)));
    }

    @DeleteMapping("/{sellerNid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeller(@PathVariable String sellerNid) {
        sellerService.delete(sellerNid);
        return ResponseEntity.noContent().build();
    }
}

package com.HeritageHub.controller;

import com.HeritageHub.model.Bidding;
import com.HeritageHub.model.enums.BidStatus;
import com.HeritageHub.service.BiddingService;
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
@RequestMapping("/api/bids")
public class BiddingController {

    private final BiddingService biddingService;

    public BiddingController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @GetMapping
    public ResponseEntity<List<Bidding>> getBids(@RequestParam(value = "productId", required = false) Long productId,
                                                 @RequestParam(value = "consumerNid", required = false) String consumerNid) {
        if (productId != null) {
            return ResponseEntity.ok(biddingService.findByProduct(productId));
        }
        if (consumerNid != null) {
            return ResponseEntity.ok(biddingService.findByConsumer(consumerNid));
        }
        return ResponseEntity.ok(biddingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bidding> getBid(@PathVariable Long id) {
        return ResponseEntity.ok(biddingService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Bidding> createBid(@RequestBody Bidding bidding,
                                             @RequestParam("consumerNid") String consumerNid,
                                             @RequestParam("productId") Long productId) {
        return ResponseEntity.ok(biddingService.create(bidding, consumerNid, productId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Bidding> updateBidStatus(@PathVariable Long id,
                                                   @RequestParam("status") BidStatus status) {
        return ResponseEntity.ok(biddingService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBid(@PathVariable Long id) {
        biddingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

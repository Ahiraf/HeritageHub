package com.HeritageHub.controller;

import com.HeritageHub.dto.ReviewResponse;
import com.HeritageHub.mapper.ReviewMapper;
import com.HeritageHub.model.Review;
import com.HeritageHub.service.ReviewService;
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
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(@RequestParam(value = "productId", required = false) Long productId) {
        List<Review> reviews = productId != null ? reviewService.findByProduct(productId) : reviewService.findAll();
        return ResponseEntity.ok(reviews.stream().map(ReviewMapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(ReviewMapper.toResponse(reviewService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@RequestBody Review review,
                                               @RequestParam("consumerNid") String consumerNid,
                                               @RequestParam("productId") Long productId) {
        return ResponseEntity.ok(ReviewMapper.toResponse(reviewService.create(review, consumerNid, productId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @RequestBody Review review) {
        return ResponseEntity.ok(ReviewMapper.toResponse(reviewService.update(id, review)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

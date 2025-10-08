package com.HeritageHub.service;

import com.HeritageHub.model.Consumer;
import com.HeritageHub.model.Product;
import com.HeritageHub.model.Review;
import com.HeritageHub.repository.ConsumerRepository;
import com.HeritageHub.repository.ProductRepository;
import com.HeritageHub.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ConsumerRepository consumerRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         ConsumerRepository consumerRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.consumerRepository = consumerRepository;
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
    }

    public List<Review> findByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return reviewRepository.findByProduct(product);
    }

    public Review create(Review review, String consumerNid, Long productId) {
        Consumer consumer = consumerRepository.findById(consumerNid)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found: " + consumerNid));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        review.setConsumer(consumer);
        review.setProduct(product);
        if (review.getReviewTime() == null) {
            review.setReviewTime(LocalDateTime.now());
        }
        return reviewRepository.save(review);
    }

    public Review update(Long reviewId, Review updates) {
        Review existing = findById(reviewId);
        existing.setReviewTime(updates.getReviewTime() != null ? updates.getReviewTime() : existing.getReviewTime());
        existing.setRating(updates.getRating());
        existing.setComment(updates.getComment());
        return reviewRepository.save(existing);
    }

    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found: " + id);
        }
        reviewRepository.deleteById(id);
    }
}

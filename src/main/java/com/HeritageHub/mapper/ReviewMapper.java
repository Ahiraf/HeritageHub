package com.HeritageHub.mapper;

import com.HeritageHub.dto.ReviewResponse;
import com.HeritageHub.model.Review;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        return new ReviewResponse(
                review.getId(),
                review.getConsumer() != null ? review.getConsumer().getConsumerNid() : null,
                review.getProduct() != null ? review.getProduct().getId() : null,
                review.getRating(),
                review.getComment(),
                review.getReviewTime()
        );
    }
}

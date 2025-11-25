package com.HeritageHub.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String consumerNid,
        Long productId,
        Integer rating,
        String comment,
        LocalDateTime reviewTime
) {}

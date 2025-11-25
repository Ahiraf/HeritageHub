package com.HeritageHub.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String productName,
        String category,
        String materialType,
        String saleType,
        String craftType,
        String color,
        BigDecimal productPrice,
        Integer inStock,
        Boolean biddable,
        String size,
        String weight,
        String productionTime,
        String description,
        String uploadImage,
        String sellerNid,
        Long approvedById,
        String approvedByName
) {}

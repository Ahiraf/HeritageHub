package com.HeritageHub.mapper;

import com.HeritageHub.dto.ProductResponse;
import com.HeritageHub.model.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCategory(),
                product.getMaterialType(),
                product.getSaleType(),
                product.getCraftType(),
                product.getColor(),
                product.getProductPrice(),
                product.getInStock(),
                product.getBiddable(),
                product.getSize(),
                product.getWeight(),
                product.getProductionTime(),
                product.getDescription(),
                product.getUploadImage(),
                product.getSeller() != null ? product.getSeller().getSellerNid() : null,
                product.getApprovedBy() != null ? product.getApprovedBy().getId() : null,
                product.getApprovedBy() != null ? product.getApprovedBy().getAdminName() : null
        );
    }
}

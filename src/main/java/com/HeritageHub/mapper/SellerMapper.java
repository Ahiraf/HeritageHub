package com.HeritageHub.mapper;

import com.HeritageHub.dto.SellerResponse;
import com.HeritageHub.model.Admin;
import com.HeritageHub.model.Seller;

public final class SellerMapper {

    private SellerMapper() {
    }

    public static SellerResponse toResponse(Seller seller) {
        if (seller == null) {
            return null;
        }
        Admin manager = seller.getManager();
        return new SellerResponse(
                seller.getSellerNid(),
                seller.getSellerName(),
                seller.getEmail(),
                seller.getPhoneNumber(),
                seller.getWorkingType(),
                seller.getDivisionName(),
                seller.getDistrictName(),
                seller.getUpazilaName(),
                seller.getVerified(),
                manager != null ? manager.getId() : null,
                manager != null ? manager.getAdminName() : null,
                seller.getApiKey()
        );
    }
}

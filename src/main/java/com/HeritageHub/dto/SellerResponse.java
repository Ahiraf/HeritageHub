package com.HeritageHub.dto;

public record SellerResponse(
        String sellerNid,
        String sellerName,
        String email,
        String phoneNumber,
        String workingType,
        String divisionName,
        String districtName,
        String upazilaName,
        Boolean verified,
        Long managerId,
        String managerName,
        String apiKey
) {}

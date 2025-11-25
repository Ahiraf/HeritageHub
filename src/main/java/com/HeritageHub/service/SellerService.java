package com.HeritageHub.service;

import com.HeritageHub.model.Admin;
import com.HeritageHub.model.Seller;
import com.HeritageHub.repository.AdminRepository;
import com.HeritageHub.repository.SellerRepository;
import com.HeritageHub.util.ApiKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;

    public SellerService(SellerRepository sellerRepository, AdminRepository adminRepository) {
        this.sellerRepository = sellerRepository;
        this.adminRepository = adminRepository;
    }

    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    public Seller findById(String sellerNid) {
        return sellerRepository.findById(sellerNid)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerNid));
    }

    public Seller create(Seller seller, Long managerId) {
        seller.setApiKey(ApiKeyGenerator.generate());
        if (managerId != null) {
            Admin admin = adminRepository.findById(managerId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + managerId));
            seller.setManager(admin);
            seller.setVerified(Boolean.TRUE);
        } else {
            seller.setVerified(Boolean.FALSE);
        }
        return sellerRepository.save(seller);
    }

    public Seller update(String sellerNid, Seller updates, Long managerId) {
        Seller existing = findById(sellerNid);
        existing.setSellerName(updates.getSellerName());
        existing.setFirstName(updates.getFirstName());
        existing.setMiddleName(updates.getMiddleName());
        existing.setLastName(updates.getLastName());
        existing.setEmail(updates.getEmail());
        existing.setPassword(updates.getPassword());
        existing.setPhoneNumber(updates.getPhoneNumber());
        existing.setSellerAddress(updates.getSellerAddress());
        existing.setVillageName(updates.getVillageName());
        existing.setUnionName(updates.getUnionName());
        existing.setUpazilaName(updates.getUpazilaName());
        existing.setDistrictName(updates.getDistrictName());
        existing.setDivisionName(updates.getDivisionName());
        existing.setPostCode(updates.getPostCode());
        existing.setWorkingType(updates.getWorkingType());
        if (updates.getVerified() != null) {
            existing.setVerified(updates.getVerified());
        }
        if (managerId != null) {
            Admin admin = adminRepository.findById(managerId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + managerId));
            existing.setManager(admin);
        }
        return sellerRepository.save(existing);
    }

    public Seller register(Seller seller, Long managerId) {
        if (seller.getSellerNid() == null || seller.getSellerNid().isBlank()) {
            throw new IllegalArgumentException("Seller NID is required.");
        }
        if (seller.getEmail() == null || seller.getEmail().isBlank()) {
            throw new IllegalArgumentException("Seller email is required.");
        }
        if (seller.getPassword() == null || seller.getPassword().isBlank()) {
            throw new IllegalArgumentException("Seller password is required.");
        }
        sellerRepository.findById(seller.getSellerNid())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Seller already exists for NID: " + existing.getSellerNid());
                });
        sellerRepository.findByEmailIgnoreCase(seller.getEmail())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Seller account already exists for email: " + existing.getEmail());
                });
        seller.setApiKey(ApiKeyGenerator.generate());
        if (managerId != null) {
            Admin admin = adminRepository.findById(managerId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + managerId));
            seller.setManager(admin);
        }
        return sellerRepository.save(seller);
    }

    public Seller authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        Seller seller = sellerRepository.findByEmailIgnoreCaseAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));
        if (seller.getApiKey() == null || seller.getApiKey().isBlank()) {
            seller.setApiKey(ApiKeyGenerator.generate());
            sellerRepository.save(seller);
        }
        return seller;
    }

    public Seller verifySeller(String sellerNid, Long adminId, boolean verified) {
        Seller seller = findById(sellerNid);
        if (adminId != null) {
            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminId));
            seller.setManager(admin);
        }
        seller.setVerified(verified);
        return sellerRepository.save(seller);
    }

    public Seller findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return sellerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found for email: " + email));
    }

    public List<Seller> findByVerified(Boolean verified) {
        return sellerRepository.findByVerified(verified);
    }

    public void updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null || email.isBlank() || newPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        Seller seller = findByEmail(email);
        seller.setPassword(newPassword);
        sellerRepository.save(seller);
    }

    @Transactional
    public void delete(String sellerNid) {
        if (!sellerRepository.existsById(sellerNid)) {
            throw new IllegalArgumentException("Seller not found: " + sellerNid);
        }
        sellerRepository.deleteById(sellerNid);
    }
}

package com.HeritageHub.service;

import com.HeritageHub.model.Admin;
import com.HeritageHub.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + id));
    }

    public Admin create(Admin admin) {
        return adminRepository.save(admin);
    }

    public Admin register(Admin admin) {
        if (admin.getEmail() == null || admin.getEmail().isBlank()) {
            throw new IllegalArgumentException("Admin email is required.");
        }
        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            throw new IllegalArgumentException("Admin password is required.");
        }
        adminRepository.findByEmailIgnoreCase(admin.getEmail())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Admin account already exists for email: " + existing.getEmail());
                });
        return adminRepository.save(admin);
    }

    public Admin authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        return adminRepository.findByEmailIgnoreCaseAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));
    }

    public Admin findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found for email: " + email));
    }

    public void updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null || email.isBlank() || newPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        Admin admin = findByEmail(email);
        admin.setPassword(newPassword);
        adminRepository.save(admin);
    }

    public Admin update(Long id, Admin updatedAdmin) {
        Admin existing = findById(id);
        existing.setAdminName(updatedAdmin.getAdminName());
        existing.setEmail(updatedAdmin.getEmail());
        existing.setPassword(updatedAdmin.getPassword());
        existing.setPhoneNumber(updatedAdmin.getPhoneNumber());
        existing.setRole(updatedAdmin.getRole());
        return adminRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new IllegalArgumentException("Admin not found: " + id);
        }
        adminRepository.deleteById(id);
    }
}

package com.HeritageHub.controller;

import com.HeritageHub.model.Admin;
import com.HeritageHub.model.Consumer;
import com.HeritageHub.model.Seller;
import com.HeritageHub.service.AdminService;
import com.HeritageHub.service.ConsumerService;
import com.HeritageHub.service.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long RESET_TOKEN_TTL_MINUTES = 15;

    private final ConsumerService consumerService;
    private final SellerService sellerService;
    private final AdminService adminService;
    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    public AuthController(ConsumerService consumerService,
                          SellerService sellerService,
                          AdminService adminService) {
        this.consumerService = consumerService;
        this.sellerService = sellerService;
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Role role = Role.from(request.role());
            return switch (role) {
                case CONSUMER -> {
                    Consumer consumer = new Consumer();
                    consumer.setConsumerNid(request.consumerNid());
                    consumer.setConsumerName(request.consumerName());
                    consumer.setEmail(request.email());
                    consumer.setPassword(request.password());
                    consumer.setPhoneNumber(request.phoneNumber());
                    consumer.setStreet(request.street());
                    consumer.setStreetNo(request.streetNo());
                    consumer.setStreetName(request.streetName());
                    consumer.setCity(request.city());
                    consumer.setCodeNo(request.codeNo());
                    Consumer saved = consumerService.register(consumer);
                    yield ResponseEntity.status(HttpStatus.CREATED).body(toConsumerProfile(saved));
                }
                case SELLER -> {
                    Seller seller = new Seller();
                    seller.setSellerNid(request.sellerNid());
                    seller.setSellerName(request.sellerName());
                    seller.setFirstName(request.sellerFirstName());
                    seller.setLastName(request.sellerLastName());
                    seller.setEmail(request.email());
                    seller.setPassword(request.password());
                    seller.setPhoneNumber(request.phoneNumber());
                    seller.setSellerAddress(request.street());
                    seller.setVillageName(request.villageName());
                    seller.setUnionName(request.unionName());
                    seller.setWorkingType(request.workingType());
                    seller.setDivisionName(request.divisionName());
                    seller.setDistrictName(request.districtName());
                    seller.setUpazilaName(request.city());
                    seller.setPostCode(request.codeNo());
                    Seller saved = sellerService.register(seller, request.managerId());
                    yield ResponseEntity.status(HttpStatus.CREATED).body(toSellerProfile(saved));
                }
                case ADMIN -> {
                    Admin admin = new Admin();
                    admin.setAdminName(request.adminName());
                    admin.setEmail(request.email());
                    admin.setPassword(request.password());
                    admin.setPhoneNumber(request.phoneNumber());
                    admin.setRole(request.adminRole());
                    Admin saved = adminService.register(admin);
                    yield ResponseEntity.status(HttpStatus.CREATED).body(toAdminProfile(saved));
                }
            };
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorPayload(ex.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            Role role = Role.from(request.role());
            String email = request.email();
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required.");
            }
            String canonicalEmail = resolveEmail(role, email);
            String code = generateResetCode();
            ResetToken token = new ResetToken(code, role, LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES));
            resetTokens.put(tokenKey(role, canonicalEmail), token);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "A verification code has been generated. For demo purposes it is returned here directly.");
            response.put("code", code);
            response.put("expiresAt", token.expiresAt().toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorPayload(ex.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            Role role = Role.from(request.role());
            String email = request.email();
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required.");
            }
            if (request.code() == null || request.code().isBlank()) {
                throw new IllegalArgumentException("Verification code is required.");
            }
            if (request.newPassword() == null || request.newPassword().isBlank()) {
                throw new IllegalArgumentException("New password is required.");
            }

            String canonicalEmail = resolveEmail(role, email);
            String key = tokenKey(role, canonicalEmail);
            ResetToken token = resetTokens.get(key);
            if (token == null || token.isExpired()) {
                throw new IllegalArgumentException("Reset code has expired or is invalid. Please request a new one.");
            }
            if (!token.code().equals(request.code().trim())) {
                throw new IllegalArgumentException("Verification code is incorrect.");
            }

            updatePassword(role, canonicalEmail, request.newPassword());
            resetTokens.remove(key);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password updated successfully.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(errorPayload(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Role role = Role.from(request.role());
            return switch (role) {
                case CONSUMER -> {
                    Consumer consumer = consumerService.authenticate(request.email(), request.password());
                    Map<String, Object> payload = toConsumerProfile(consumer);
                    payload.put("message", "Login successful.");
                    payload.put("role", Role.CONSUMER.name());
                    yield ResponseEntity.ok(payload);
                }
                case SELLER -> {
                    Seller seller = sellerService.authenticate(request.email(), request.password());
                    Map<String, Object> payload = toSellerProfile(seller);
                    payload.put("message", "Login successful.");
                    payload.put("role", Role.SELLER.name());
                    yield ResponseEntity.ok(payload);
                }
                case ADMIN -> {
                    Admin admin = adminService.authenticate(request.email(), request.password());
                    Map<String, Object> payload = toAdminProfile(admin);
                    payload.put("message", "Login successful.");
                    payload.put("role", Role.ADMIN.name());
                    yield ResponseEntity.ok(payload);
                }
            };
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorPayload(ex.getMessage()));
        }
    }

    private Map<String, Object> toConsumerProfile(Consumer consumer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("consumerNid", consumer.getConsumerNid());
        payload.put("consumerName", consumer.getConsumerName());
        payload.put("email", consumer.getEmail());
        payload.put("phoneNumber", consumer.getPhoneNumber());
        payload.put("street", consumer.getStreet());
        payload.put("streetNo", consumer.getStreetNo());
        payload.put("streetName", consumer.getStreetName());
        payload.put("city", consumer.getCity());
        payload.put("codeNo", consumer.getCodeNo());
        return payload;
    }

    private Map<String, Object> toSellerProfile(Seller seller) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sellerNid", seller.getSellerNid());
        payload.put("sellerName", seller.getSellerName());
        payload.put("sellerFirstName", seller.getFirstName());
        payload.put("sellerLastName", seller.getLastName());
        payload.put("email", seller.getEmail());
        payload.put("phoneNumber", seller.getPhoneNumber());
        payload.put("sellerAddress", seller.getSellerAddress());
        payload.put("address", seller.getSellerAddress());
        payload.put("villageName", seller.getVillageName());
        payload.put("unionName", seller.getUnionName());
        payload.put("workingType", seller.getWorkingType());
        payload.put("divisionName", seller.getDivisionName());
        payload.put("division", seller.getDivisionName());
        payload.put("districtName", seller.getDistrictName());
        payload.put("district", seller.getDistrictName());
        payload.put("city", seller.getUpazilaName());
        payload.put("upazila", seller.getUpazilaName());
        payload.put("postCode", seller.getPostCode());
        payload.put("managerId", seller.getManager() != null ? seller.getManager().getId() : null);
        return payload;
    }

    private Map<String, Object> toAdminProfile(Admin admin) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("adminId", admin.getId());
        payload.put("adminName", admin.getAdminName());
        payload.put("email", admin.getEmail());
        payload.put("phoneNumber", admin.getPhoneNumber());
        payload.put("role", admin.getRole());
        return payload;
    }

    private String resolveEmail(Role role, String email) {
        return switch (role) {
            case CONSUMER -> consumerService.findByEmail(email).getEmail();
            case SELLER -> sellerService.findByEmail(email).getEmail();
            case ADMIN -> adminService.findByEmail(email).getEmail();
        };
    }

    private void updatePassword(Role role, String email, String newPassword) {
        switch (role) {
            case CONSUMER -> consumerService.updatePassword(email, newPassword);
            case SELLER -> sellerService.updatePassword(email, newPassword);
            case ADMIN -> adminService.updatePassword(email, newPassword);
        }
    }

    private String tokenKey(Role role, String email) {
        return role.name() + ":" + email.trim().toLowerCase();
    }

    private String generateResetCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private Map<String, Object> errorPayload(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("error", message);
        return payload;
    }

    enum Role {
        ADMIN, SELLER, CONSUMER;

        static Role from(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Role is required.");
            }
            try {
                return Role.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported role: " + value);
            }
        }
    }

    public record RegisterRequest(
            String role,
            String adminName,
            String adminRole,
            String sellerNid,
            String sellerName,
            String sellerFirstName,
            String sellerLastName,
            String workingType,
            String divisionName,
            String districtName,
            String unionName,
            String villageName,
            String city,
            String consumerNid,
            String consumerName,
            String street,
            String streetNo,
            String streetName,
            String codeNo,
            String phoneNumber,
            String email,
            String password,
            Long managerId
    ) {
    }

    public record LoginRequest(String role, String email, String password) {
    }

    public record ForgotPasswordRequest(String role, String email) {
    }

    public record ResetPasswordRequest(String role, String email, String code, String newPassword) {
    }

    private record ResetToken(String code, Role role, LocalDateTime expiresAt) {
        boolean isExpired() {
            return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
        }
    }
}

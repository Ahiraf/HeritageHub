package com.HeritageHub.service;

import com.HeritageHub.model.Consumer;
import com.HeritageHub.repository.ConsumerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumerService {

    private final ConsumerRepository consumerRepository;

    public ConsumerService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    public List<Consumer> findAll() {
        return consumerRepository.findAll();
    }

    public Consumer findById(String consumerNid) {
        return consumerRepository.findById(consumerNid)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found: " + consumerNid));
    }

    public Consumer create(Consumer consumer) {
        return consumerRepository.save(consumer);
    }

    public Consumer register(Consumer consumer) {
        if (consumer.getEmail() == null || consumer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required for registration.");
        }
        if (consumer.getPassword() == null || consumer.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for registration.");
        }
        consumerRepository.findByEmailIgnoreCase(consumer.getEmail())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("An account already exists for email: " + existing.getEmail());
                });
        return consumerRepository.save(consumer);
    }

    public Consumer authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        return consumerRepository.findByEmailIgnoreCaseAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));
    }

    public Consumer findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return consumerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Consumer not found for email: " + email));
    }

    public void updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null || email.isBlank() || newPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        Consumer consumer = findByEmail(email);
        consumer.setPassword(newPassword);
        consumerRepository.save(consumer);
    }

    public Consumer update(String consumerNid, Consumer updates) {
        Consumer existing = findById(consumerNid);
        existing.setConsumerName(updates.getConsumerName());
        existing.setFirstName(updates.getFirstName());
        existing.setMiddleName(updates.getMiddleName());
        existing.setLastName(updates.getLastName());
        existing.setEmail(updates.getEmail());
        existing.setPassword(updates.getPassword());
        existing.setPhoneNumber(updates.getPhoneNumber());
        existing.setStreet(updates.getStreet());
        existing.setStreetNo(updates.getStreetNo());
        existing.setStreetName(updates.getStreetName());
        existing.setCity(updates.getCity());
        existing.setCodeNo(updates.getCodeNo());
        return consumerRepository.save(existing);
    }

    @Transactional
    public void delete(String consumerNid) {
        if (!consumerRepository.existsById(consumerNid)) {
            throw new IllegalArgumentException("Consumer not found: " + consumerNid);
        }
        consumerRepository.deleteById(consumerNid);
    }
}

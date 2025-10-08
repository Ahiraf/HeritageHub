package com.HeritageHub.controller;

import com.HeritageHub.model.Consumer;
import com.HeritageHub.service.ConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping
    public ResponseEntity<List<Consumer>> getConsumers() {
        return ResponseEntity.ok(consumerService.findAll());
    }

    @GetMapping("/{consumerNid}")
    public ResponseEntity<Consumer> getConsumer(@PathVariable String consumerNid) {
        return ResponseEntity.ok(consumerService.findById(consumerNid));
    }

    @PostMapping
    public ResponseEntity<Consumer> createConsumer(@RequestBody Consumer consumer) {
        return ResponseEntity.ok(consumerService.create(consumer));
    }

    @PutMapping("/{consumerNid}")
    public ResponseEntity<Consumer> updateConsumer(@PathVariable String consumerNid,
                                                   @RequestBody Consumer consumer) {
        return ResponseEntity.ok(consumerService.update(consumerNid, consumer));
    }

    @DeleteMapping("/{consumerNid}")
    public ResponseEntity<Void> deleteConsumer(@PathVariable String consumerNid) {
        consumerService.delete(consumerNid);
        return ResponseEntity.noContent().build();
    }
}

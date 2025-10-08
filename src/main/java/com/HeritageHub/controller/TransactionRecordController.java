package com.HeritageHub.controller;

import com.HeritageHub.model.TransactionRecord;
import com.HeritageHub.model.enums.TransactionMedium;
import com.HeritageHub.service.TransactionRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionRecordController {

    private final TransactionRecordService transactionService;

    public TransactionRecordController(TransactionRecordService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionRecord>> getTransactions(@RequestParam(value = "medium", required = false) TransactionMedium medium) {
        if (medium != null) {
            return ResponseEntity.ok(transactionService.findByMedium(medium));
        }
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionRecord> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TransactionRecord> createTransaction(@RequestBody TransactionRecord record,
                                                               @RequestParam("orderId") Long orderId) {
        return ResponseEntity.ok(transactionService.create(record, orderId));
    }
}

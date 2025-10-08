package com.HeritageHub.service;

import com.HeritageHub.model.CustomerOrder;
import com.HeritageHub.model.TransactionRecord;
import com.HeritageHub.model.enums.TransactionMedium;
import com.HeritageHub.repository.CustomerOrderRepository;
import com.HeritageHub.repository.TransactionRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionRecordService {

    private final TransactionRecordRepository transactionRepository;
    private final CustomerOrderRepository orderRepository;

    public TransactionRecordService(TransactionRecordRepository transactionRepository,
                                    CustomerOrderRepository orderRepository) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
    }

    public List<TransactionRecord> findAll() {
        return transactionRepository.findAll();
    }

    public TransactionRecord findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
    }

    public List<TransactionRecord> findByMedium(TransactionMedium medium) {
        return transactionRepository.findByTransactionMedium(medium);
    }

    public TransactionRecord create(TransactionRecord record, Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        record.setOrder(order);
        if (record.getTransactionDate() == null) {
            record.setTransactionDate(LocalDateTime.now());
        }
        TransactionRecord saved = transactionRepository.save(record);
        order.setTransaction(saved);
        orderRepository.save(order);
        return saved;
    }
}

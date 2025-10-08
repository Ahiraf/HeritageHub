package com.HeritageHub.repository;

import com.HeritageHub.model.TransactionRecord;
import com.HeritageHub.model.enums.TransactionMedium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByTransactionMedium(TransactionMedium medium);
}

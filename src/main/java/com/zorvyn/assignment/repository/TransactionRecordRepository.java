package com.zorvyn.assignment.repository;

import com.zorvyn.assignment.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    
}

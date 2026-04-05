package com.zorvyn.assignment.service;
import com.zorvyn.assignment.entity.TransactionRecord;
import java.util.List;
import org.springframework.stereotype.Service;

import com.zorvyn.assignment.repository.TransactionRecordRepository;

@Service
public class TransactionRecordService {

    private TransactionRecordRepository transactionRecordRepository;

        public TransactionRecordService (TransactionRecordRepository transactionRecordRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
    }  
    
    
    public TransactionRecord createRecord(TransactionRecord record) 
    {
    
        return transactionRecordRepository.save(record);
    }


    public TransactionRecord getRecordById(Long id) {
        return transactionRecordRepository.findById(id).orElse(null);
    }
    
    public List<TransactionRecord> getAllRecords() {
        return transactionRecordRepository.findAll();
    }
    
    public TransactionRecord updateRecord(Long id, TransactionRecord updatedRecord) {
        TransactionRecord existingRecord = transactionRecordRepository.findById(id).orElse(null);
        if (existingRecord != null) {
            existingRecord.setAmount(updatedRecord.getAmount());
            existingRecord.setDate(updatedRecord.getDate());
            existingRecord.setType(updatedRecord.getType());
            existingRecord.setCategory(updatedRecord.getCategory());
            return transactionRecordRepository.save(existingRecord);
        }
        return null;
    }

    public boolean deleteRecord(Long id) {
        if (transactionRecordRepository.existsById(id)) {
            transactionRecordRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
}

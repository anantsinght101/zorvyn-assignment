package com.zorvyn.assignment.service;
import com.zorvyn.assignment.dto.SummaryResponseDTO;
import com.zorvyn.assignment.entity.TransactionRecord;
import java.time.LocalDate;
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


   public List<TransactionRecord> getAllRecords(
        TransactionRecord.Type type,
        TransactionRecord.Category category,
        LocalDate startDate,
        LocalDate endDate
) {
    return transactionRecordRepository.findAll().stream()
        .filter(r -> type == null || r.getType() == type)
        .filter(r -> category == null || r.getCategory() == category)
        .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
        .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
        .toList();
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



        //summary methods 
        
        public double totalIncome(LocalDate startDate, LocalDate endDate) {
    return transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == TransactionRecord.Type.INCOME)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .mapToDouble(TransactionRecord::getAmount)
        .sum();

        }

        public double totalExpense(LocalDate startDate, LocalDate endDate) {
    return transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == TransactionRecord.Type.EXPENSE)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .mapToDouble(TransactionRecord::getAmount)
        .sum();
        }

        public long recordCount(LocalDate startDate, LocalDate endDate) {
    return transactionRecordRepository.findAll().stream()
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .count();
        }

        public double totalByCategory(TransactionRecord.Category category, LocalDate startDate, LocalDate endDate) {
    return transactionRecordRepository.findAll().stream()
        .filter(r -> r.getCategory() == category)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .mapToDouble(TransactionRecord::getAmount)
        .sum();
        }

        public double totalByType(TransactionRecord.Type type, LocalDate startDate, LocalDate endDate) {
    return transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == type)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .mapToDouble(TransactionRecord::getAmount)
        .sum();
        }

        public double netspend(LocalDate startDate, LocalDate endDate) {
    double income = totalIncome(startDate, endDate);
    double expense = totalExpense(startDate, endDate);
    return income - expense;
        }





public double averageIncome(LocalDate startDate, LocalDate endDate) {
    List<TransactionRecord> records = transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == TransactionRecord.Type.INCOME)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .toList();

    double total = records.stream().mapToDouble(TransactionRecord::getAmount).sum();
    return records.size() > 0 ? total / records.size() : 0;
}

public double averageSpend(LocalDate startDate, LocalDate endDate) {
    List<TransactionRecord> records = transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == TransactionRecord.Type.EXPENSE)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .toList();

    double total = records.stream().mapToDouble(TransactionRecord::getAmount).sum();
    return records.size() > 0 ? total / records.size() : 0;
}





public SummaryResponseDTO getSummary(LocalDate startDate, LocalDate endDate) {
    double income = totalIncome(startDate, endDate);
    double expense = totalExpense(startDate, endDate);
    long totalCount = recordCount(startDate, endDate);

    long incomeCount = transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == TransactionRecord.Type.INCOME)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .count();

    long expenseCount = transactionRecordRepository.findAll().stream()
        .filter(r -> r.getType() == TransactionRecord.Type.EXPENSE)
        .filter(r -> !r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
        .count();

    double net = income - expense;
    double avgIncome = averageIncome(startDate, endDate);
    double avgExpense = averageSpend(startDate, endDate);

    return new SummaryResponseDTO(
        income, expense, net, totalCount,
        incomeCount, expenseCount,
        avgIncome, avgExpense
    );
}
}

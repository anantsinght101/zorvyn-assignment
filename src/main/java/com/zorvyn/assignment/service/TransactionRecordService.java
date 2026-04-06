package com.zorvyn.assignment.service;

import com.zorvyn.assignment.dto.SummaryResponseDTO;
import com.zorvyn.assignment.entity.TransactionRecord;
import com.zorvyn.assignment.exception.ResourceNotFoundException;
import com.zorvyn.assignment.repository.TransactionRecordRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionRecordService {

    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionRecordService(TransactionRecordRepository transactionRecordRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
    }

    public TransactionRecord createRecord(TransactionRecord record) {
        return transactionRecordRepository.save(record);
    }

    // 👉 Now correctly passes filters and pagination to the repository
    public Page<TransactionRecord> getAllRecords(
            TransactionRecord.Type type,
            TransactionRecord.Category category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return transactionRecordRepository.findFilteredAndPaged(type, category, startDate, endDate, pageable);
    }

    public TransactionRecord getById(Long id) {
        return transactionRecordRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
    }

    public TransactionRecord updateRecord(Long id, TransactionRecord updatedRecord) {
        TransactionRecord existing = transactionRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction record not found with id: " + id));

        existing.setAmount(updatedRecord.getAmount());
        existing.setDate(updatedRecord.getDate());
        existing.setType(updatedRecord.getType());
        existing.setCategory(updatedRecord.getCategory());
        existing.setDescription(updatedRecord.getDescription());

        return transactionRecordRepository.save(existing);
    }

    public boolean deleteRecord(Long id) {
        TransactionRecord record = transactionRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));

        record.setDeleted(true);
        transactionRecordRepository.save(record);
        return true;
    }

    public TransactionRecord restoreRecord(Long id) {
        TransactionRecord record = transactionRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));

        record.setDeleted(false);
        return transactionRecordRepository.save(record);
    }

    public List<TransactionRecord> getDeletedRecords() {
        return transactionRecordRepository.findByDeletedTrue();
    }

    // 👉 Added this method so your Controller doesn't throw errors
    public Map<String, Long> getRecordCounts() {
        List<TransactionRecord> activeRecords = transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).getContent();
        long income = activeRecords.stream().filter(r -> r.getType() == TransactionRecord.Type.INCOME).count();
        long expense = activeRecords.stream().filter(r -> r.getType() == TransactionRecord.Type.EXPENSE).count();
        return Map.of("total", (long) activeRecords.size(), "income", income, "expense", expense);
    }

    // 👉 Added this method so your Controller doesn't throw errors
    public List<TransactionRecord> getRecentRecords(int limit) {
        return transactionRecordRepository.findByDeletedFalse(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"))
        ).getContent();
    }

    public Map<String, Double> getCategoryTotals(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (TransactionRecord.Category category : TransactionRecord.Category.values()) {
            double total = totalByCategory(category, startDate, endDate);
            categoryTotals.put(category.name(), total);
        }

        return categoryTotals;
    }

    public double totalIncome(LocalDate startDate, LocalDate endDate) {
        return transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> r.getType() == TransactionRecord.Type.INCOME)
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .mapToDouble(TransactionRecord::getAmount)
                .sum();
    }

    public double totalExpense(LocalDate startDate, LocalDate endDate) {
        return transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> r.getType() == TransactionRecord.Type.EXPENSE)
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .mapToDouble(TransactionRecord::getAmount)
                .sum();
    }

    public long recordCount(LocalDate startDate, LocalDate endDate) {
        return transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .count();
    }

    public double totalByCategory(TransactionRecord.Category category, LocalDate startDate, LocalDate endDate) {
        return transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> r.getCategory() == category)
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .mapToDouble(TransactionRecord::getAmount)
                .sum();
    }

    public double totalByType(TransactionRecord.Type type, LocalDate startDate, LocalDate endDate) {
        return transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> r.getType() == type)
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .mapToDouble(TransactionRecord::getAmount)
                .sum();
    }

    public double netspend(LocalDate startDate, LocalDate endDate) {
        double income = totalIncome(startDate, endDate);
        double expense = totalExpense(startDate, endDate);
        return income - expense;
    }

    public double averageIncome(LocalDate startDate, LocalDate endDate) {
        List<TransactionRecord> records = transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> r.getType() == TransactionRecord.Type.INCOME)
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .toList();

        double total = records.stream().mapToDouble(TransactionRecord::getAmount).sum();
        return records.size() > 0 ? total / records.size() : 0;
    }

    public double averageSpend(LocalDate startDate, LocalDate endDate) {
        List<TransactionRecord> records = transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> r.getType() == TransactionRecord.Type.EXPENSE)
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .toList();

        double total = records.stream().mapToDouble(TransactionRecord::getAmount).sum();
        return records.size() > 0 ? total / records.size() : 0;
    }

    public SummaryResponseDTO getSummary(LocalDate startDate, LocalDate endDate) {

        List<TransactionRecord> records = transactionRecordRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .filter(r -> startDate == null || !r.getDate().isBefore(startDate))
                .filter(r -> endDate == null || !r.getDate().isAfter(endDate))
                .toList();

        double income = records.stream()
                .filter(r -> r.getType() == TransactionRecord.Type.INCOME)
                .mapToDouble(TransactionRecord::getAmount).sum();

        double expense = records.stream()
                .filter(r -> r.getType() == TransactionRecord.Type.EXPENSE)
                .mapToDouble(TransactionRecord::getAmount).sum();

        double net = income - expense;

        long totalCount = records.size();
        long incomeCount = records.stream().filter(r -> r.getType() == TransactionRecord.Type.INCOME).count();
        long expenseCount = records.stream().filter(r -> r.getType() == TransactionRecord.Type.EXPENSE).count();

        double avgIncome = incomeCount > 0 ? income / incomeCount : 0;
        double avgExpense = expenseCount > 0 ? expense / expenseCount : 0;

        Map<String, Double> categoryTotals = new HashMap<>();
        for (TransactionRecord.Category cat : TransactionRecord.Category.values()) {
            double total = records.stream()
                    .filter(r -> r.getCategory() == cat)
                    .mapToDouble(TransactionRecord::getAmount).sum();
            categoryTotals.put(cat.name(), total);
        }

        return new SummaryResponseDTO(
                income, expense, net, totalCount,
                incomeCount, expenseCount,
                avgIncome, avgExpense, categoryTotals
        );
    }
}
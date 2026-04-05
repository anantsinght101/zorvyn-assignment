package com.zorvyn.assignment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zorvyn.assignment.dto.SummaryRequestDTO;
import com.zorvyn.assignment.dto.SummaryResponseDTO;
import com.zorvyn.assignment.entity.TransactionRecord;
import com.zorvyn.assignment.service.TransactionRecordService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionRecordController {

    private TransactionRecordService transactionRecordService;

    public TransactionRecordController(TransactionRecordService transactionRecordService) {
        this.transactionRecordService = transactionRecordService;
    }

    @PostMapping
    public TransactionRecord create(@RequestBody TransactionRecord record) {
        return transactionRecordService.createRecord(record);
    }

    @GetMapping
    public List<TransactionRecord> getAll() {
        return transactionRecordService.getAllRecords();
    }

    @GetMapping("/{id}")
    public TransactionRecord getById(@PathVariable Long id) {
        return transactionRecordService.getAllRecords().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @PutMapping("/{id}")
    public TransactionRecord update(@PathVariable Long id, @RequestBody TransactionRecord record) {
        return transactionRecordService.updateRecord(id, record);
    }

    @PostMapping("/{id}/delete")
    public boolean delete(@PathVariable Long id) {
        return transactionRecordService.deleteRecord(id);
    }

    @PostMapping("/summary")
    public SummaryResponseDTO getSummary(@RequestBody SummaryRequestDTO request) {
        return transactionRecordService.getSummary(
                request.getStartDate(),
                request.getEndDate());
    }

}

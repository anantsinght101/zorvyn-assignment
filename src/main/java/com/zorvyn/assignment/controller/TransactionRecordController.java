package com.zorvyn.assignment.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public TransactionRecord create(@RequestBody TransactionRecord record) {
        return transactionRecordService.createRecord(record);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping
    public Page<TransactionRecord> getAll(
            @RequestParam(required = false) TransactionRecord.Type type,
            @RequestParam(required = false) TransactionRecord.Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period,
            Pageable pageable, // 👉 Spring Boot automatically populates this from URL parameters (e.g., ?page=0&size=10&sort=date,desc)
            Authentication authentication) {

        boolean isStrictViewer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")) &&
                authentication.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANALYST"));

        if (isStrictViewer && (startDate != null || endDate != null)) {
            throw new AccessDeniedException("Viewers are restricted to period analytics and cannot query exact dates.");
        }

        if (period != null && !period.isEmpty()) {
            endDate = LocalDate.now();
            switch (period.toUpperCase()) {
                case "WEEKLY":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "MONTHLY":
                    startDate = endDate.minusMonths(1);
                    break;
                case "QUARTERLY":
                    startDate = endDate.minusMonths(3);
                    break;
                case "HALF_YEARLY":
                    startDate = endDate.minusMonths(6);
                    break;
                case "ANNUALLY":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    break;
            }
        }

        return transactionRecordService.getAllRecords(type, category, startDate, endDate, pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/count")
    public ResponseEntity<java.util.Map<String, Long>> getCount() {
        return ResponseEntity.ok(transactionRecordService.getRecordCounts());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/recordCount")
    public ResponseEntity<Map<String, Long>> recordCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period,
            Authentication authentication) {
        LocalDateRange range = resolveRangeOrThrow(startDate, endDate, period, authentication);
        long count = transactionRecordService.recordCount(range.startDate(), range.endDate());
        return ResponseEntity.ok(Map.of("value", count));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/totalIncome")
    public ResponseEntity<Map<String, Double>> totalIncome(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period,
            Authentication authentication) {
        LocalDateRange range = resolveRangeOrThrow(startDate, endDate, period, authentication);
        double value = transactionRecordService.totalIncome(range.startDate(), range.endDate());
        return ResponseEntity.ok(Map.of("value", value));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/totalExpense")
    public ResponseEntity<Map<String, Double>> totalExpense(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period,
            Authentication authentication) {
        LocalDateRange range = resolveRangeOrThrow(startDate, endDate, period, authentication);
        double value = transactionRecordService.totalExpense(range.startDate(), range.endDate());
        return ResponseEntity.ok(Map.of("value", value));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/netspend")
    public ResponseEntity<Map<String, Double>> netspend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String period,
            Authentication authentication) {
        LocalDateRange range = resolveRangeOrThrow(startDate, endDate, period, authentication);
        double value = transactionRecordService.netspend(range.startDate(), range.endDate());
        return ResponseEntity.ok(Map.of("value", value));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/recent")
    public ResponseEntity<List<TransactionRecord>> getRecent(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(transactionRecordService.getRecentRecords(limit));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionRecordService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TransactionRecord update(@PathVariable Long id, @RequestBody TransactionRecord record) {
        return transactionRecordService.updateRecord(id, record);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/restore")
    public TransactionRecord restore(@PathVariable Long id) {
        return transactionRecordService.restoreRecord(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleted")
    public List<TransactionRecord> getDeleted() {
        return transactionRecordService.getDeletedRecords();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @PostMapping("/summary")
    public SummaryResponseDTO getSummary(@RequestBody SummaryRequestDTO request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        String period = request.getPeriod();

        if (period != null && !period.isEmpty()) {
            endDate = LocalDate.now();
            switch (period.toUpperCase()) {
                case "WEEKLY":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "MONTHLY":
                    startDate = endDate.minusMonths(1);
                    break;
                case "QUARTERLY":
                    startDate = endDate.minusMonths(3);
                    break;
                case "HALF_YEARLY":
                    startDate = endDate.minusMonths(6);
                    break;
                case "ANNUALLY":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    endDate = null;
                    startDate = null;
                    break;
            }
        }

        return transactionRecordService.getSummary(startDate, endDate);
    }

    private boolean isStrictViewer(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER"))
                && authentication.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANALYST"));
    }

    private LocalDateRange resolveRangeOrThrow(LocalDate startDate, LocalDate endDate, String period,
            Authentication authentication) {
        if (isStrictViewer(authentication) && (startDate != null || endDate != null)) {
            throw new AccessDeniedException("Viewers are restricted to period analytics and cannot query exact dates.");
        }

        if (period != null && !period.isEmpty()) {
            endDate = LocalDate.now();
            switch (period.toUpperCase()) {
                case "WEEKLY":
                    startDate = endDate.minusWeeks(1);
                    break;
                case "MONTHLY":
                    startDate = endDate.minusMonths(1);
                    break;
                case "QUARTERLY":
                    startDate = endDate.minusMonths(3);
                    break;
                case "HALF_YEARLY":
                    startDate = endDate.minusMonths(6);
                    break;
                case "ANNUALLY":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    startDate = null;
                    endDate = null;
                    break;
            }
        }

        return new LocalDateRange(startDate, endDate);
    }

    private record LocalDateRange(LocalDate startDate, LocalDate endDate) {
    }
}
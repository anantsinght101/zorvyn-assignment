package com.zorvyn.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SummaryResponseDTO {
    private double totalIncome;
    private double totalExpense;
    private double netBalance;
    private long recordCount;      // total records
    private long incomeCount;      // optional but good
    private long expenseCount;     // optional but good
    private double averageIncome;
    private double averageExpense;
}


    


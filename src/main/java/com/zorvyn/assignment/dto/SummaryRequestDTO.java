package com.zorvyn.assignment.dto;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SummaryRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
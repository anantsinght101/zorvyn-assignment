package com.zorvyn.assignment.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRecord {

    
    public enum Type {
    INCOME, EXPENSE
}

public enum Category {
    FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, OTHER
}   

    @Id
    @GeneratedValue
    private Long id;
    private double amount;
    private java.time.LocalDate date;    
  @Enumerated(EnumType.STRING)
private Type type;

@Enumerated(EnumType.STRING)
private Category category;
    

}
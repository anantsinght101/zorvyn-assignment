package com.zorvyn.assignment.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Positive;
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
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Positive
    private double amount;

    @Column(nullable = false)
    private java.time.LocalDate date;    
  @Enumerated(EnumType.STRING)
private Type type;

@Enumerated(EnumType.STRING)
private Category category;

     @Column(nullable = false)
    private boolean deleted = false;

    private String description;

   
    

}
package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private BankCard fromCard;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private BankCard toCard;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING
    }
}
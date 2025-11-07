package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_cards")
@Getter
@Setter
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "card_number_encrypted", nullable = false)
    private String cardNumberEncrypted;

    @NotBlank
    @Column(name = "card_holder", nullable = false)
    private String cardHolder;

    @NotBlank
    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;

    @Column(name = "cvv_encrypted")
    private String cvvEncrypted;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Person user;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }
}
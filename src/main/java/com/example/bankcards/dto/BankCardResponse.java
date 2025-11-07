package com.example.bankcards.dto;

import com.example.bankcards.entity.BankCard;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BankCardResponse {
    private Integer id;
    private String maskedCardNumber;
    private String cardHolder;
    private String expirationDate;
    private BigDecimal balance;
    private BankCard.CardStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
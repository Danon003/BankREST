package com.example.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private Integer transactionId;
    private String fromCardMaskedNumber;
    private String toCardMaskedNumber;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String status;
    private String description;
}
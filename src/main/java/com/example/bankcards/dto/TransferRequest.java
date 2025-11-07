package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "ID карты отправителя обязателен")
    private Integer fromCardId;

    @NotNull(message = "ID карты получателя обязателен")
    private Integer toCardId;

    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    private BigDecimal amount;

    private String description;
}
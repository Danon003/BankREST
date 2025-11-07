package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankCardRequest {

    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @NotBlank(message = "Имя владельца обязательно")
    @Size(min = 2, max = 100, message = "Имя владельца должно быть от 2 до 100 символов")
    private String cardHolder;

    @NotBlank(message = "Срок действия обязателен")
    @Pattern(regexp = "(0[1-9]|1[0-2])/[0-9]{2}", message = "Формат срока действия: ММ/ГГ")
    private String expirationDate;

    @NotBlank(message = "CVV обязателен")
    @Pattern(regexp = "\\d{3}", message = "CVV должен содержать 3 цифры")
    private String cvv;

    @NotNull(message = "Баланс обязателен")
    private BigDecimal initialBalance;
}

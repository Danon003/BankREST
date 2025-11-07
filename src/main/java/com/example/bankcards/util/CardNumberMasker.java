package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    public String maskCardNumber(String encryptedCardNumber, EncryptionUtil encryptionUtil) {
        try {
            String decrypted = encryptionUtil.decrypt(encryptedCardNumber);
            return maskCardNumber(decrypted);
        } catch (Exception e) {
            return "**** **** **** ****";
        }
    }
}
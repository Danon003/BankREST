package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    @Value("${app.encryption.secret:MyBankCardEncryptionKey123}")
    private String secretKey;

    private static final String ALGORITHM = "AES";

    public String encrypt(String data) {
        try {
            byte[] keyData = secretKey.getBytes();
            byte[] paddedKey = new byte[16];
            System.arraycopy(keyData, 0, paddedKey, 0, Math.min(keyData.length, 16));

            SecretKeySpec keySpec = new SecretKeySpec(paddedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            byte[] keyData = secretKey.getBytes();
            byte[] paddedKey = new byte[16];
            System.arraycopy(keyData, 0, paddedKey, 0, Math.min(keyData.length, 16));

            SecretKeySpec keySpec = new SecretKeySpec(paddedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}
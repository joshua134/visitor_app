package com.visitor.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
@Component
public class EncryptionUtil implements AttributeConverter<String, String> {
    
    private static String secretKeyValue;
    
    @Value("${encryption.secret.key}")
    public void setSecretKey(String key) {
        secretKeyValue = key;
    }
    
    private static final String ALGORITHM = "AES";
    
    // Generate a proper AES key from any string input
    private SecretKeySpec getKey() {
        try {
            byte[] keyBytes;
            
            // If the key is already a valid length (16, 24, or 32 bytes)
            if (secretKeyValue.getBytes().length == 16 || 
                secretKeyValue.getBytes().length == 24 || 
                secretKeyValue.getBytes().length == 32) {
                keyBytes = secretKeyValue.getBytes();
            } else {
                // Use SHA-256 to generate a 32-byte key from any input
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                keyBytes = sha.digest(secretKeyValue.getBytes(StandardCharsets.UTF_8));
            }
            
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            log.error("Failed to generate encryption key: {}", e.getMessage());
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        
        try {
            SecretKeySpec key = getKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            log.error("Encryption error: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to encrypt data", ex);
        }
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        
        try {
            SecretKeySpec key = getKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Decryption error: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to decrypt data", ex);
        }
    }
}
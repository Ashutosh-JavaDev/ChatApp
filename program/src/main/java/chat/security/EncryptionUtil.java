package main.java.chat.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting messages.
 */
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    
    /**
     * Generates a new AES encryption key.
     * 
     * @return The generated secret key encoded in Base64
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error generating encryption key", e);
        }
    }
    
    /**
     * Encrypts a message using AES encryption.
     * 
     * @param message The message to encrypt
     * @param keyStr The encryption key in Base64 encoding
     * @return The encrypted message in the format "iv:encryptedData" in Base64 encoding
     */
    public static String encrypt(String message, String keyStr) {
        try {
            // Decode the key
            byte[] keyBytes = Base64.getDecoder().decode(keyStr);
            SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // Generate a random IV
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Encrypt the message
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(message.getBytes());
            
            // Return IV and encrypted data in Base64
            String ivStr = Base64.getEncoder().encodeToString(iv);
            String encryptedStr = Base64.getEncoder().encodeToString(encrypted);
            return ivStr + ":" + encryptedStr;
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting message", e);
        }
    }
    
    /**
     * Decrypts a message using AES encryption.
     * 
     * @param encryptedMessage The encrypted message in the format "iv:encryptedData" in Base64 encoding
     * @param keyStr The encryption key in Base64 encoding
     * @return The decrypted message
     */
    public static String decrypt(String encryptedMessage, String keyStr) {
        try {
            // Split the encrypted message into IV and data
            String[] parts = encryptedMessage.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted message format");
            }
            
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedData = Base64.getDecoder().decode(parts[1]);
            
            // Decode the key
            byte[] keyBytes = Base64.getDecoder().decode(keyStr);
            SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // Decrypt the message
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encryptedData);
            
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting message", e);
        }
    }
}
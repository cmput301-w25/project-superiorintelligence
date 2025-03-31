package com.example.superior_intelligence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for secure password hashing using SHA-256 algorithm.
 */
public class PasswordHasher {

    /**
     * Generates a SHA-256 hash of the input password.
     * @param password The plaintext password to hash
     * @return Hexadecimal string representation of the hashed password
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    // Hashes the password using SHA-256.
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     * @param bytes The byte array to convert
     * @return Hexadecimal string representation of the byte array
     */
    // Converts a byte array to a hexadecimal string.
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}

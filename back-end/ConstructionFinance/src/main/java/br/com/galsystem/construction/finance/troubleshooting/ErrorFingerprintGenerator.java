package br.com.galsystem.construction.finance.troubleshooting;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class ErrorFingerprintGenerator {

    private static final int FINGERPRINT_LENGTH = 16;

    public String generate(String exceptionClass, String path, String message) {
        String normalized = normalize(exceptionClass, path, message);
        String hash = generateSHA256(normalized);
        return hash.substring(0, Math.min(FINGERPRINT_LENGTH, hash.length()));
    }

    private String normalize(String exceptionClass, String path, String message) {
        String normalizedPath = normalizePath(path);
        String normalizedMessage = normalizeMessage(message);
        return exceptionClass + "|" + normalizedPath + "|" + normalizedMessage;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.replaceAll("\\d+", "N")
                .replaceAll("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", "UUID");
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        return message
                .replaceAll("\\d+", "N")
                .replaceAll("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", "UUID")
                .replaceAll("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", "TIMESTAMP")
                .replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "EMAIL");
    }

    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

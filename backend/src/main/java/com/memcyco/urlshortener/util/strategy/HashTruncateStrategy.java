package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashTruncateStrategy implements CodeGenerationStrategy {

    private static final String CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public String generate(String originalUrl, ShortLink partialEntity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(7);
            for (int i = 0; i < 7; i++) {
                int index = (hash[i] & 0xFF) % 62;
                sb.append(CHARS.charAt(index));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;

public class SequentialStrategy implements CodeGenerationStrategy {

    private static final String CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public String generate(String originalUrl, ShortLink partialEntity) {
        Long id = partialEntity.getId();
        if (id == null) {
            throw new IllegalStateException("SequentialStrategy requires a persisted entity");
        }
        return encodeId(id);
    }

    private String encodeId(long id) {
        if (id == 0) {
            return String.valueOf(CHARS.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        long value = id;
        while (value > 0) {
            sb.append(CHARS.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}

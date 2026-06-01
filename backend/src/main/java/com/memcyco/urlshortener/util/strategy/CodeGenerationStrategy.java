package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;

public interface CodeGenerationStrategy {

    String generate(String originalUrl, ShortLink partialEntity);
}

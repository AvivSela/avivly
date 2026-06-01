package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;
import com.memcyco.urlshortener.util.Base62;

public class RandomBase62Strategy implements CodeGenerationStrategy {

    @Override
    public String generate(String originalUrl, ShortLink partialEntity) {
        return Base62.generate(7);
    }
}

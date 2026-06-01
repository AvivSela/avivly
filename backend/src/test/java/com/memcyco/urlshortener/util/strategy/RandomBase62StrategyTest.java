package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomBase62StrategyTest {

    private final RandomBase62Strategy strategy = new RandomBase62Strategy();
    private final ShortLink dummy = ShortLink.builder().originalUrl("https://example.com").build();

    @Test
    void generate_isNotNull() {
        assertThat(strategy.generate("https://example.com", dummy)).isNotNull();
    }

    @Test
    void generate_hasCorrectLength() {
        assertThat(strategy.generate("https://example.com", dummy)).hasSize(7);
    }

    @Test
    void generate_containsOnlyBase62Chars() {
        assertThat(strategy.generate("https://example.com", dummy)).matches("[a-zA-Z0-9]+");
    }
}

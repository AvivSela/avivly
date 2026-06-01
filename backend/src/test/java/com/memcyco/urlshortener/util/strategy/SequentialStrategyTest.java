package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SequentialStrategyTest {

    private final SequentialStrategy strategy = new SequentialStrategy();

    private String encode(long id) {
        ShortLink link = ShortLink.builder().id(id).originalUrl("https://example.com").build();
        return strategy.generate("https://example.com", link);
    }

    @Test
    void encodeId_zero_returnsFirstChar() {
        assertThat(encode(0)).isEqualTo("a");
    }

    @Test
    void encodeId_one_returnsSecondChar() {
        assertThat(encode(1)).isEqualTo("b");
    }

    @Test
    void encodeId_61_returnsLastChar() {
        // CHARS[61] = '9' (last char of "...0123456789")
        assertThat(encode(61)).isEqualTo("9");
    }

    @Test
    void encodeId_62_rollsOver() {
        // 62 = 1*62 + 0 → "ba"
        assertThat(encode(62)).isEqualTo("ba");
    }

    @Test
    void generate_nullId_throwsIllegalState() {
        ShortLink link = ShortLink.builder().originalUrl("https://example.com").build();
        assertThatThrownBy(() -> strategy.generate("https://example.com", link))
                .isInstanceOf(IllegalStateException.class);
    }
}

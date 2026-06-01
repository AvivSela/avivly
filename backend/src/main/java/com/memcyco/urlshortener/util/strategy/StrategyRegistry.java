package com.memcyco.urlshortener.util.strategy;

import com.memcyco.urlshortener.model.ShortLink;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class StrategyRegistry {

    private final Map<StrategyType, CodeGenerationStrategy> strategies;

    public StrategyRegistry() {
        strategies = new EnumMap<>(StrategyType.class);
        strategies.put(StrategyType.RANDOM_BASE62, new RandomBase62Strategy());
        strategies.put(StrategyType.HASH_TRUNCATE, new HashTruncateStrategy());
        strategies.put(StrategyType.SEQUENTIAL, new SequentialStrategy());
    }

    public String generate(StrategyType type, String url, ShortLink entity) {
        CodeGenerationStrategy strategy = strategies.getOrDefault(type, strategies.get(StrategyType.RANDOM_BASE62));
        return strategy.generate(url, entity);
    }
}

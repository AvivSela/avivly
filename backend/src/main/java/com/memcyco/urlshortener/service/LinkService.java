package com.memcyco.urlshortener.service;

import com.memcyco.urlshortener.dto.CreateLinkRequest;
import com.memcyco.urlshortener.dto.UpdateLinkRequest;
import com.memcyco.urlshortener.model.ShortLink;
import com.memcyco.urlshortener.repository.ShortLinkRepository;
import com.memcyco.urlshortener.util.strategy.StrategyRegistry;
import com.memcyco.urlshortener.util.strategy.StrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final ShortLinkRepository repo;
    private final StrategyRegistry strategyRegistry;

    @Cacheable(value = "shortLinks", key = "#shortCode")
    public ShortLink findByShortCode(String shortCode) {
        return repo.findByShortCode(shortCode).orElse(null);
    }

    public List<ShortLink> findAll() {
        return repo.findAll();
    }

    @Transactional
    public ShortLink create(CreateLinkRequest req) {
        String strategyName = req.strategy();
        StrategyType strategyType;
        try {
            strategyType = (strategyName != null && !strategyName.isBlank())
                ? StrategyType.valueOf(strategyName)
                : StrategyType.RANDOM_BASE62;
        } catch (IllegalArgumentException e) {
            strategyType = StrategyType.RANDOM_BASE62;
        }

        ShortLink partialEntity = ShortLink.builder()
            .originalUrl(req.originalUrl())
            .strategy(strategyType.name())
            .maxClicks(req.maxClicks())
            .expiresAt(req.expiresAt())
            .tags(req.tags())
            .build();

        String code = (req.customAlias() != null && !req.customAlias().isBlank())
            ? req.customAlias()
            : strategyRegistry.generate(strategyType, req.originalUrl(), partialEntity);

        if (repo.findByShortCode(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Short code already taken: " + code);
        }

        partialEntity.setShortCode(code);
        return repo.save(partialEntity);
    }

    @Transactional
    @CacheEvict(value = "shortLinks", key = "#result.shortCode")
    public ShortLink update(Long id, UpdateLinkRequest req) {
        ShortLink link = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found: " + id));

        if (req.originalUrl() != null) link.setOriginalUrl(req.originalUrl());
        if (req.isActive() != null) link.setActive(req.isActive());
        if (req.expiresAt() != null) link.setExpiresAt(req.expiresAt());
        if (req.tags() != null) link.setTags(req.tags());
        if (req.maxClicks() != null) link.setMaxClicks(req.maxClicks());

        return repo.save(link);
    }

    @Transactional
    public void delete(Long id) {
        ShortLink link = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found: " + id));
        evictCache(link.getShortCode());
        repo.delete(link);
    }

    @CacheEvict(value = "shortLinks", key = "#shortCode")
    public void evictCache(String shortCode) {}

    @Transactional
    @CacheEvict(value = "shortLinks", key = "#shortCode")
    public void recordClick(String shortCode) {
        repo.incrementClicks(shortCode);
    }
}

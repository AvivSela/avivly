package com.memcyco.urlshortener.service;

import com.memcyco.urlshortener.dto.AnalyticsResponse;
import com.memcyco.urlshortener.model.ClickAnalytics;
import com.memcyco.urlshortener.repository.ClickAnalyticsRepository;
import com.memcyco.urlshortener.repository.ShortLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickAnalyticsRepository clickRepo;
    private final ShortLinkRepository linkRepo;

    @Async
    @Transactional
    public void logClickAsync(String shortCode, String referer, String userAgent, String ip) {
        clickRepo.save(ClickAnalytics.builder()
            .shortCode(shortCode)
            .referer(referer)
            .userAgent(userAgent)
            .ipAddress(ip)
            .build());
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        int totalClicks = linkRepo.findByShortCode(shortCode)
            .map(l -> l.getTotalClicks())
            .orElse(0);

        List<AnalyticsResponse.DailyCount> clicksOverTime = clickRepo.countClicksByDay(shortCode)
            .stream()
            .map(row -> new AnalyticsResponse.DailyCount(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? ((Number) row[1]).longValue() : 0L
            ))
            .toList();

        List<AnalyticsResponse.ReferrerCount> topReferrers = clickRepo.topReferrers(shortCode)
            .stream()
            .map(row -> new AnalyticsResponse.ReferrerCount(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? ((Number) row[1]).longValue() : 0L
            ))
            .toList();

        List<AnalyticsResponse.AgentCount> topUserAgents = clickRepo.topUserAgents(shortCode)
            .stream()
            .map(row -> new AnalyticsResponse.AgentCount(
                row[0] != null ? row[0].toString() : "",
                row[1] != null ? ((Number) row[1]).longValue() : 0L
            ))
            .toList();

        return new AnalyticsResponse(totalClicks, clicksOverTime, topReferrers, topUserAgents,
                List.of(), List.of());
    }
}

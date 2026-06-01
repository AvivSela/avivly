package com.memcyco.urlshortener.dto;

import java.util.List;

public record AnalyticsResponse(
    int totalClicks,
    List<DailyCount> clicksOverTime,
    List<ReferrerCount> topReferrers,
    List<AgentCount> topUserAgents
) {
    public record DailyCount(String date, long count) {}
    public record ReferrerCount(String referer, long count) {}
    public record AgentCount(String userAgent, long count) {}
}

package com.memcyco.urlshortener.repository;

import com.memcyco.urlshortener.model.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    List<ClickAnalytics> findByShortCodeOrderByClickedAtDesc(String shortCode);

    @Query("SELECT CAST(c.clickedAt AS date) as date, COUNT(c) as count FROM ClickAnalytics c WHERE c.shortCode = :shortCode GROUP BY CAST(c.clickedAt AS date) ORDER BY CAST(c.clickedAt AS date)")
    List<Object[]> countClicksByDay(@Param("shortCode") String shortCode);

    @Query("SELECT c.referer, COUNT(c) as count FROM ClickAnalytics c WHERE c.shortCode = :shortCode AND c.referer IS NOT NULL GROUP BY c.referer ORDER BY count DESC")
    List<Object[]> topReferrers(@Param("shortCode") String shortCode);

    @Query("SELECT c.userAgent, COUNT(c) as count FROM ClickAnalytics c WHERE c.shortCode = :shortCode AND c.userAgent IS NOT NULL GROUP BY c.userAgent ORDER BY count DESC")
    List<Object[]> topUserAgents(@Param("shortCode") String shortCode);
}

package com.memcyco.urlshortener.controller;

import com.memcyco.urlshortener.dto.AnalyticsResponse;
import com.memcyco.urlshortener.dto.CreateLinkRequest;
import com.memcyco.urlshortener.dto.UpdateLinkRequest;
import com.memcyco.urlshortener.model.ShortLink;
import com.memcyco.urlshortener.service.AnalyticsService;
import com.memcyco.urlshortener.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private final AnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<ShortLink> create(@Valid @RequestBody CreateLinkRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(linkService.create(req));
    }

    @GetMapping
    public List<ShortLink> getAll() {
        return linkService.findAll();
    }

    @PutMapping("/{id}")
    public ShortLink update(@PathVariable Long id, @Valid @RequestBody UpdateLinkRequest req) {
        return linkService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        linkService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shortCode}/analytics")
    public AnalyticsResponse getAnalytics(@PathVariable String shortCode) {
        return analyticsService.getAnalytics(shortCode);
    }
}

package com.memcyco.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateLinkRequest(
    @NotBlank String originalUrl,
    String customAlias,
    String strategy,
    Integer maxClicks,
    LocalDateTime expiresAt,
    String tags
) {}

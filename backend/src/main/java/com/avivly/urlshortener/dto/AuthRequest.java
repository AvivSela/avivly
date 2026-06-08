package com.avivly.urlshortener.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
) {}

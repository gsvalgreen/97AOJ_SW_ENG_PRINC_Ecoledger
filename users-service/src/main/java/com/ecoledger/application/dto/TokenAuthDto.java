package com.ecoledger.application.dto;

public record TokenAuthDto(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}

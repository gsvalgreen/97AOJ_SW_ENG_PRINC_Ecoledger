package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.config.ProducerApprovalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProducerApprovalTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerApprovalTokenProvider.class);

    private final ProducerApprovalProperties properties;
    private final JwtEncoder jwtEncoder;

    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiry;

    public ProducerApprovalTokenProvider(ProducerApprovalProperties properties, JwtEncoder jwtEncoder) {
        this.properties = properties;
        this.jwtEncoder = jwtEncoder;
    }

    public String currentToken() {
        Instant now = Instant.now();
        Instant validUntil = cachedTokenExpiry;
        if (cachedToken == null || validUntil == null || now.isAfter(validUntil.minusSeconds(15))) {
            synchronized (this) {
                if (cachedToken == null || cachedTokenExpiry == null || now.isAfter(cachedTokenExpiry.minusSeconds(15))) {
                    generateToken();
                }
            }
        }
        return cachedToken;
    }

    private void generateToken() {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.tokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(properties.clientId())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("scopes", properties.scopes())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        this.cachedToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        this.cachedTokenExpiry = expiresAt;
        LOGGER.info("Generated producer approval JWT expiring at {}", expiresAt);
    }
}

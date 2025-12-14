package com.ecoledger.application.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyEntity {

    @Id
    @Column(name = "idempotency_key")
    private String key;

    @Column(nullable = false)
    private UUID cadastroId;

    private Instant createdAt;

    public IdempotencyEntity() {}

    public IdempotencyEntity(String key, UUID cadastroId) {
        this.key = key;
        this.cadastroId = cadastroId;
        this.createdAt = Instant.now();
    }

    public String getKey() { return key; }
    public UUID getCadastroId() { return cadastroId; }
    public Instant getCreatedAt() { return createdAt; }
}

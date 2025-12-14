package com.ecoledger.certificacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alteracoes_selo")
public class AlteracaoSelo {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "producer_id", nullable = false, length = 64)
    private String producerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "de_status", length = 20)
    private SeloStatus deStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "para_status", nullable = false, length = 20)
    private SeloStatus paraStatus;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "evidencia", length = 500)
    private String evidencia;

    protected AlteracaoSelo() {
        // JPA
    }

    private AlteracaoSelo(UUID id, String producerId, SeloStatus deStatus, SeloStatus paraStatus,
                          String motivo, String evidencia) {
        this.id = id;
        this.producerId = producerId;
        this.deStatus = deStatus;
        this.paraStatus = paraStatus;
        this.motivo = motivo;
        this.evidencia = evidencia;
        this.createdAt = Instant.now();
    }

    public static AlteracaoSelo fromTransition(String producerId, SeloStatus deStatus, SeloStatus paraStatus,
                                               String motivo, String evidencia) {
        return new AlteracaoSelo(UUID.randomUUID(), producerId, deStatus, paraStatus, motivo, evidencia);
    }

    public UUID getId() {
        return id;
    }

    public String getProducerId() {
        return producerId;
    }

    public SeloStatus getDeStatus() {
        return deStatus;
    }

    public SeloStatus getParaStatus() {
        return paraStatus;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getEvidencia() {
        return evidencia;
    }
}

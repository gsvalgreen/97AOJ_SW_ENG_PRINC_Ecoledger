package com.ecoledger.auditoria.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an audit record for a movimentacao.
 * This is an append-only record - once created, the base audit data is immutable.
 * Only manual revisions can update the resultado and add observations.
 */
@Entity
@Table(name = "registro_auditorias", indexes = {
    @Index(name = "idx_auditoria_movimentacao", columnList = "movimentacao_id"),
    @Index(name = "idx_auditoria_producer", columnList = "producer_id"),
    @Index(name = "idx_auditoria_processado_em", columnList = "processado_em")
})
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "movimentacao_id", nullable = false)
    private UUID movimentacaoId;

    @Column(name = "producer_id", nullable = false)
    private String producerId;

    @Column(name = "versao_regra", nullable = false)
    private String versaoRegra;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false)
    private ResultadoAuditoria resultado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "auditoria_evidencias", joinColumns = @JoinColumn(name = "auditoria_id"))
    private List<Evidencia> evidencias = new ArrayList<>();

    @Column(name = "processado_em", nullable = false)
    private Instant processadoEm;

    @Column(name = "auditor_id")
    private String auditorId;

    @Column(name = "observacoes", length = 4000)
    private String observacoes;

    @Column(name = "revisado_em")
    private Instant revisadoEm;

    protected RegistroAuditoria() {
        // JPA constructor
    }

    /**
     * Creates a new audit record from automatic validation.
     */
    public RegistroAuditoria(UUID movimentacaoId, String producerId, String versaoRegra,
                             ResultadoAuditoria resultado, List<Evidencia> evidencias) {
        this.movimentacaoId = Objects.requireNonNull(movimentacaoId, "movimentacaoId is required");
        this.producerId = Objects.requireNonNull(producerId, "producerId is required");
        this.versaoRegra = Objects.requireNonNull(versaoRegra, "versaoRegra is required");
        this.resultado = Objects.requireNonNull(resultado, "resultado is required");
        this.evidencias = evidencias != null ? new ArrayList<>(evidencias) : new ArrayList<>();
        this.processadoEm = Instant.now();
    }

    /**
     * Applies a manual revision by an auditor.
     * Can only change result from REQUER_REVISAO to APROVADO or REPROVADO.
     */
    public void aplicarRevisao(String auditorId, ResultadoAuditoria novoResultado, String observacoes) {
        Objects.requireNonNull(auditorId, "auditorId is required");
        Objects.requireNonNull(novoResultado, "novoResultado is required");
        
        if (novoResultado == ResultadoAuditoria.REQUER_REVISAO) {
            throw new IllegalArgumentException("Cannot set result to REQUER_REVISAO in manual revision");
        }
        
        this.auditorId = auditorId;
        this.resultado = novoResultado;
        this.observacoes = observacoes;
        this.revisadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMovimentacaoId() {
        return movimentacaoId;
    }

    public String getProducerId() {
        return producerId;
    }

    public String getVersaoRegra() {
        return versaoRegra;
    }

    public ResultadoAuditoria getResultado() {
        return resultado;
    }

    public List<Evidencia> getEvidencias() {
        return Collections.unmodifiableList(evidencias);
    }

    public Instant getProcessadoEm() {
        return processadoEm;
    }

    public String getAuditorId() {
        return auditorId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Instant getRevisadoEm() {
        return revisadoEm;
    }

    public boolean foiRevisado() {
        return revisadoEm != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroAuditoria that = (RegistroAuditoria) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

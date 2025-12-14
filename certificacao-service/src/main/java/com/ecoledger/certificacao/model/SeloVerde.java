package com.ecoledger.certificacao.model;

import com.ecoledger.certificacao.messaging.event.ResultadoAuditoria;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "selos")
public class SeloVerde {

    @Id
    @Column(name = "producer_id", nullable = false, length = 64)
    private String producerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeloStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", length = 20)
    private SeloNivel nivel;

    @Column(name = "pontuacao", nullable = false)
    private int pontuacao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "selo_motivos", joinColumns = @JoinColumn(name = "selo_producer_id"))
    @Column(name = "motivo", nullable = false, length = 255)
    private List<String> motivos = new ArrayList<>();

    @Column(name = "versao_regra", length = 50)
    private String versaoRegra;

    @Column(name = "ultima_auditoria_id")
    private UUID ultimaAuditoriaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ultimo_resultado_auditoria", length = 30)
    private ResultadoAuditoria ultimoResultadoAuditoria;

    @Column(name = "ultimo_check", nullable = false)
    private Instant ultimoCheck;

    @Column(name = "expiracao_em")
    private Instant expiracaoEm;

    protected SeloVerde() {
        // JPA
    }

    public SeloVerde(String producerId, SeloStatus status, SeloNivel nivel, int pontuacao,
                     List<String> motivos, String versaoRegra, UUID ultimaAuditoriaId,
                     ResultadoAuditoria ultimoResultadoAuditoria, Instant ultimoCheck, Instant expiracaoEm) {
        this.producerId = producerId;
        this.status = status;
        this.nivel = nivel;
        this.pontuacao = pontuacao;
        if (motivos != null) {
            this.motivos = new ArrayList<>(motivos);
        }
        this.versaoRegra = versaoRegra;
        this.ultimaAuditoriaId = ultimaAuditoriaId;
        this.ultimoResultadoAuditoria = ultimoResultadoAuditoria;
        this.ultimoCheck = ultimoCheck;
        this.expiracaoEm = expiracaoEm;
    }

    public void atualizar(SeloStatus status, SeloNivel nivel, int pontuacao, List<String> novosMotivos,
                          String versaoRegra, UUID ultimaAuditoriaId, ResultadoAuditoria ultimoResultadoAuditoria,
                          Instant expiracaoEm) {
        this.status = status;
        this.nivel = nivel;
        this.pontuacao = pontuacao;
        this.motivos.clear();
        if (novosMotivos != null) {
            this.motivos.addAll(novosMotivos);
        }
        this.versaoRegra = versaoRegra;
        this.ultimaAuditoriaId = ultimaAuditoriaId;
        this.ultimoResultadoAuditoria = ultimoResultadoAuditoria;
        this.ultimoCheck = Instant.now();
        this.expiracaoEm = expiracaoEm;
    }

    public boolean expirado(Instant agora) {
        return expiracaoEm != null && agora.isAfter(expiracaoEm);
    }

    public String getProducerId() {
        return producerId;
    }

    public SeloStatus getStatus() {
        return status;
    }

    public SeloNivel getNivel() {
        return nivel;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public List<String> getMotivos() {
        return Collections.unmodifiableList(motivos);
    }

    public String getVersaoRegra() {
        return versaoRegra;
    }

    public UUID getUltimaAuditoriaId() {
        return ultimaAuditoriaId;
    }

    public ResultadoAuditoria getUltimoResultadoAuditoria() {
        return ultimoResultadoAuditoria;
    }

    public Instant getUltimoCheck() {
        return ultimoCheck;
    }

    public Instant getExpiracaoEm() {
        return expiracaoEm;
    }
}

package com.ecoledger.movimentacao.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "producer_id", nullable = false)
    private String producerId;

    @Column(name = "commodity_id", nullable = false)
    private String commodityId;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "quantidade", nullable = false)
    private BigDecimal quantidade;

    @Column(name = "unidade", nullable = false)
    private String unidade;

    @Column(name = "registro_timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "movimentacao_id")
    private List<MovimentacaoAnexo> anexos = new ArrayList<>();

    protected Movimentacao() {
        // for JPA
    }

    public Movimentacao(String producerId,
                         String commodityId,
                         String tipo,
                         BigDecimal quantidade,
                         String unidade,
                         OffsetDateTime timestamp,
                         Double latitude,
                         Double longitude,
                         List<MovimentacaoAnexo> anexos) {
        this.producerId = producerId;
        this.commodityId = commodityId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.criadoEm = OffsetDateTime.now();
        if (anexos != null) {
            this.anexos.addAll(anexos);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProducerId() {
        return producerId;
    }

    public String getCommodityId() {
        return commodityId;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public List<MovimentacaoAnexo> getAnexos() {
        return Collections.unmodifiableList(anexos);
    }
}

package com.ecoledger.auditoria.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Embeddable value object representing evidence collected during audit.
 */
@Embeddable
public class Evidencia {

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "detalhe", nullable = false, length = 2000)
    private String detalhe;

    protected Evidencia() {
        // JPA constructor
    }

    public Evidencia(String tipo, String detalhe) {
        this.tipo = Objects.requireNonNull(tipo, "tipo is required");
        this.detalhe = Objects.requireNonNull(detalhe, "detalhe is required");
    }

    public String getTipo() {
        return tipo;
    }

    public String getDetalhe() {
        return detalhe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evidencia evidencia = (Evidencia) o;
        return Objects.equals(tipo, evidencia.tipo) && Objects.equals(detalhe, evidencia.detalhe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, detalhe);
    }

    @Override
    public String toString() {
        return "Evidencia{tipo='%s', detalhe='%s'}".formatted(tipo, detalhe);
    }
}

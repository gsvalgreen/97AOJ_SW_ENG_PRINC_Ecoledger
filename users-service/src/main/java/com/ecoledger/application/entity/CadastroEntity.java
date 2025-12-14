package com.ecoledger.application.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cadastros")
public class CadastroEntity {

    @Id
    private String id;

    private String status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity candidatoUsuario;

    private Instant submetidoEm;

    public CadastroEntity() {
        this.id = UUID.randomUUID().toString();
        this.submetidoEm = Instant.now();
        this.status = "PENDENTE";
    }

    // getters e setters
    public String getId() { return id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UsuarioEntity getCandidatoUsuario() { return candidatoUsuario; }
    public void setCandidatoUsuario(UsuarioEntity candidatoUsuario) { this.candidatoUsuario = candidatoUsuario; }
    public Instant getSubmetidoEm() { return submetidoEm; }
    public void setSubmetidoEm(Instant submetidoEm) { this.submetidoEm = submetidoEm; }
}


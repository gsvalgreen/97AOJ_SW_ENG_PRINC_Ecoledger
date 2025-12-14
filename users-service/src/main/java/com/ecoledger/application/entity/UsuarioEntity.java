package com.ecoledger.application.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    private String id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    private String role;

    private String documento;

    private String status;

    private Instant criadoEm;

    public UsuarioEntity() {
        this.id = UUID.randomUUID().toString();
        this.criadoEm = Instant.now();
    }

    // getters e setters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}

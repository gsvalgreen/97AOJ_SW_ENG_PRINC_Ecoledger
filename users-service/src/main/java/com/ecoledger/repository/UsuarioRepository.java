package com.ecoledger.repository;

import com.ecoledger.application.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, String> {
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByDocumento(String documento);
}
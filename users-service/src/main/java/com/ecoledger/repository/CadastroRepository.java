package com.ecoledger.repository;

import com.ecoledger.application.entity.CadastroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CadastroRepository extends JpaRepository<CadastroEntity, UUID> {}


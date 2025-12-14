package com.ecoledger.repository;

import com.ecoledger.application.entity.CadastroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CadastroRepository extends JpaRepository<CadastroEntity, String> {}


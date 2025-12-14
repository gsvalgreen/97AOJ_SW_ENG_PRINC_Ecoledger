package com.ecoledger.certificacao.repository;

import com.ecoledger.certificacao.model.AlteracaoSelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlteracaoSeloRepository extends JpaRepository<AlteracaoSelo, UUID> {
    List<AlteracaoSelo> findByProducerIdOrderByCreatedAtDesc(String producerId);
}

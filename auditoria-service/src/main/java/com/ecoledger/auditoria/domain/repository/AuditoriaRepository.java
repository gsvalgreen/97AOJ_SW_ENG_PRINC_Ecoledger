package com.ecoledger.auditoria.domain.repository;

import com.ecoledger.auditoria.domain.model.RegistroAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RegistroAuditoria entities.
 */
@Repository
public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, UUID> {

    /**
     * Find all audit records for a specific producer, ordered by processadoEm descending.
     */
    List<RegistroAuditoria> findByProducerIdOrderByProcessadoEmDesc(String producerId);

    /**
     * Find all audit records for a specific producer with pagination.
     */
    Page<RegistroAuditoria> findByProducerId(String producerId, Pageable pageable);

    /**
     * Find the most recent audit record for a specific movimentacao.
     */
    Optional<RegistroAuditoria> findTopByMovimentacaoIdOrderByProcessadoEmDesc(UUID movimentacaoId);

    /**
     * Find all audit records for a specific movimentacao.
     */
    List<RegistroAuditoria> findByMovimentacaoIdOrderByProcessadoEmDesc(UUID movimentacaoId);

    /**
     * Check if an audit record exists for a specific movimentacao.
     */
    boolean existsByMovimentacaoId(UUID movimentacaoId);
}

package com.ecoledger.movimentacao.domain.repository;

import com.ecoledger.movimentacao.domain.model.IdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, java.util.UUID> {
    Optional<IdempotencyRecord> findByIdempotencyKey(String key);
    Optional<IdempotencyRecord> findByRequestHash(String requestHash);
}

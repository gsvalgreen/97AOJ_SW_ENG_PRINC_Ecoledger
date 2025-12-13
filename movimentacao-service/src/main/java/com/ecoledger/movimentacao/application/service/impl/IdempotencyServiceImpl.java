package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.dto.MovimentacaoResponse;
import com.ecoledger.movimentacao.application.service.IdempotencyService;
import com.ecoledger.movimentacao.domain.model.IdempotencyRecord;
import com.ecoledger.movimentacao.domain.repository.IdempotencyRecordRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyServiceImpl(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    private String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public Optional<MovimentacaoResponse> handle(String idempotencyKey, String requestHash, java.util.concurrent.Callable<java.util.UUID> createOperation) throws Exception {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyRecord> existing = repository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                var r = existing.get();
                if ("COMPLETED".equals(r.getStatus())) {
                    // return stored response
                    var body = r.getResponseBody();
                    // stored body is just UUID string
                    return Optional.of(new MovimentacaoResponse(java.util.UUID.fromString(body)));
                }
                // in-progress: fallthrough to avoid blocking; return empty to indicate caller should proceed
                return Optional.empty();
            }
            // create a record in-progress
            IdempotencyRecord rec = new IdempotencyRecord(idempotencyKey, requestHash, "IN_PROGRESS", OffsetDateTime.now());
            repository.save(rec);
            try {
                java.util.UUID id = createOperation.call();
                rec.setResponseBody(id.toString());
                rec.setStatus("COMPLETED");
                repository.save(rec);
                return Optional.of(new MovimentacaoResponse(id));
            } catch (Exception ex) {
                // mark failed and rethrow
                rec.setStatus("FAILED");
                repository.save(rec);
                throw ex;
            }
        } else {
            // fallback dedup by requestHash
            Optional<IdempotencyRecord> existing = repository.findByRequestHash(requestHash);
            if (existing.isPresent() && "COMPLETED".equals(existing.get().getStatus())) {
                return Optional.of(new MovimentacaoResponse(java.util.UUID.fromString(existing.get().getResponseBody())));
            }
            // no key and not found: perform operation and store a record (no key)
            java.util.UUID id = createOperation.call();
            IdempotencyRecord rec = new IdempotencyRecord(null, requestHash, "COMPLETED", OffsetDateTime.now());
            rec.setResponseBody(id.toString());
            repository.save(rec);
            return Optional.of(new MovimentacaoResponse(id));
        }
    }
}

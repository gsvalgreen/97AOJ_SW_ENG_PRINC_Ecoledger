package com.ecoledger.application.service.impl;

import com.ecoledger.application.entity.IdempotencyEntity;
import com.ecoledger.repository.IdempotencyRepository;
import com.ecoledger.application.service.IdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRepository repo;

    public IdempotencyServiceImpl(IdempotencyRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<String> findCadastroIdByKey(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return repo.findById(key).map(IdempotencyEntity::getCadastroId);
    }

    @Override
    public void saveKey(String key, String cadastroId) {
        if (key == null || key.isBlank()) return;
        repo.save(new IdempotencyEntity(key, cadastroId));
    }
}


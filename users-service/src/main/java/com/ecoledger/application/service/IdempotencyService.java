package com.ecoledger.application.service;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyService {
    Optional<UUID> findCadastroIdByKey(String key);
    void saveKey(String key, UUID cadastroId);
}


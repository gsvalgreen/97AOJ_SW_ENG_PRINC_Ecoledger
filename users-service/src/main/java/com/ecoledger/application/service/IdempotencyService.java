package com.ecoledger.application.service;

import java.util.Optional;

public interface IdempotencyService {
    Optional<String> findCadastroIdByKey(String key);
    void saveKey(String key, String cadastroId);
}


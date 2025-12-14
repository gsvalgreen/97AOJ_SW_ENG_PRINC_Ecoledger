package com.ecoledger.repository;

import com.ecoledger.application.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRepository extends JpaRepository<IdempotencyEntity, String> {}


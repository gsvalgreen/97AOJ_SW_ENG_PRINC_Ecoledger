package com.ecoledger.movimentacao.domain.repository;

import com.ecoledger.movimentacao.domain.model.Movimentacao;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, UUID> {
}


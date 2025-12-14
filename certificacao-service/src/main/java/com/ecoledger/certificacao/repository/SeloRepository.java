package com.ecoledger.certificacao.repository;

import com.ecoledger.certificacao.model.SeloVerde;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeloRepository extends JpaRepository<SeloVerde, String> {
}

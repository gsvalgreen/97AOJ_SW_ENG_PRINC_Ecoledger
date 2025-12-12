package com.ecoledger.movimentacao.domain.repository;

import com.ecoledger.movimentacao.domain.model.Movimentacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, UUID> {
    Page<Movimentacao> findByProducerId(String producerId, Pageable pageable);
    Page<Movimentacao> findByProducerIdAndCommodityId(String producerId, String commodityId, Pageable pageable);
    Page<Movimentacao> findByProducerIdAndTimestampBetween(String producerId, java.time.OffsetDateTime from, java.time.OffsetDateTime to, Pageable pageable);
    Page<Movimentacao> findByProducerIdAndTimestampAfter(String producerId, java.time.OffsetDateTime from, Pageable pageable);
    Page<Movimentacao> findByProducerIdAndTimestampBefore(String producerId, java.time.OffsetDateTime to, Pageable pageable);
    List<Movimentacao> findByCommodityIdOrderByTimestampDesc(String commodityId);
}


package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoRequest;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.model.MovimentacaoAnexo;
import com.ecoledger.movimentacao.domain.repository.MovimentacaoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository repository;

    public MovimentacaoService(MovimentacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UUID registrar(MovimentacaoRequest request) {
        Movimentacao movimentacao = new Movimentacao(
                request.producerId(),
                request.commodityId(),
                request.tipo(),
                request.quantidade(),
                request.unidade(),
                request.timestamp(),
                request.latitude(),
                request.longitude(),
                buildAnexos(request.anexos())
        );
        return repository.save(movimentacao).getId();
    }

    private List<MovimentacaoAnexo> buildAnexos(List<MovimentacaoRequest.MovimentacaoRequestAttachment> anexos) {
        if (anexos == null) {
            return List.of();
        }
        return anexos.stream()
                .map(anexo -> {
                    MovimentacaoAnexo entity = new MovimentacaoAnexo();
                    entity.setTipo(anexo.tipo());
                    entity.setUrl(anexo.url());
                    entity.setHash(anexo.hash());
                    return entity;
                })
                .toList();
    }
}


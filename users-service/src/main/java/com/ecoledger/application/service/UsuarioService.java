package com.ecoledger.application.service;


import com.ecoledger.application.dto.*;

public interface UsuarioService {
    RespostaCadastroDto createCadastro(String idempotencyKey, CadastroCriacaoDto dto);
    CadastroDto getCadastro(String id);
    TokenAuthDto authenticate(String email, String password);
    UsuarioDto getUsuario(String id);
    UsuarioDto updateUsuario(String id, UsuarioAtualizacaoDto dto);
    UsuarioDto updateStatus(String id, String status, String reason);
}



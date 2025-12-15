package com.ecoledger.application.controller;

import com.ecoledger.application.dto.*;
import com.ecoledger.application.service.UsuarioService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuariosControllerTest {

    @Test
    public void controller_basic_paths() {
        UsuarioService svc = mock(UsuarioService.class);
        UsuariosController ctrl = new UsuariosController(svc);

        var dto = new CadastroCriacaoDto("nome","email@example.com","DOC123","senha123","produtor", Map.<String,Object>of("fazenda","x"), List.<Map<String,Object>>of());
        var resp = new RespostaCadastroDto("cid","PENDING");
        when(svc.createCadastro("key", dto)).thenReturn(resp);

        var r = ctrl.createCadastro("key", dto);
        assertEquals(201, r.getStatusCodeValue());
        assertEquals(resp, r.getBody());

        var user = new UsuarioDto("uid","nome","email","role","doc","status", Instant.now());
        var cadastro = new CadastroDto("cid","PENDING", user, Instant.now());
        when(svc.getCadastro("cid")).thenReturn(cadastro);
        var gr = ctrl.getCadastro("cid");
        assertEquals(200, gr.getStatusCodeValue());
        assertEquals(cadastro, gr.getBody());

        var token = new TokenAuthDto("a","b",100L);
        when(svc.authenticate("e","p")).thenReturn(token);
        var lr = new UsuariosController.LoginRequest("e","p");
        var lg = ctrl.login(lr);
        assertEquals(token, lg.getBody());

        when(svc.getUsuario("uid")).thenReturn(user);
        var gu = ctrl.getUsuario("uid");
        assertEquals(user, gu.getBody());

        when(svc.updateUsuario(eq("uid"), any())).thenReturn(user);
        var upd = new UsuarioAtualizacaoDto("nome", Map.of());
        var pu = ctrl.patchUsuario("uid", upd);
        assertEquals(user, pu.getBody());

        when(svc.updateStatus("uid","APROVADO","reason")).thenReturn(user);
        var req = new UsuariosController.StatusUpdateRequest("APROVADO","reason");
        var ps = ctrl.patchStatus("uid", req);
        assertEquals(user, ps.getBody());
    }
}

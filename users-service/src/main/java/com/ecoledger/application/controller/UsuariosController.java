package com.ecoledger.application.controller;

import com.ecoledger.application.dto.*;
import com.ecoledger.application.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {

    private final UsuarioService service;

    public UsuariosController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/cadastros")
    public ResponseEntity<RespostaCadastroDto> createCadastro(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CadastroCriacaoDto dto) {
        var resp = service.createCadastro(idempotencyKey, dto);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/cadastros/{id}")
    public ResponseEntity<CadastroDto> getCadastro(@PathVariable String id) {
        return ResponseEntity.ok(service.getCadastro(id));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<TokenAuthDto> login(@RequestBody LoginRequest login) {
        return ResponseEntity.ok(service.authenticate(login.email(), login.password()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> getUsuario(@PathVariable String id) {
        return ResponseEntity.ok(service.getUsuario(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioDto> patchUsuario(@PathVariable String id, @RequestBody UsuarioAtualizacaoDto dto) {
        return ResponseEntity.ok(service.updateUsuario(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioDto> patchStatus(@PathVariable String id, @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(service.updateStatus(id, req.status(), req.reason()));
    }

    // pequenos records internos para requests não descritos como componentes no DTOs acima
    public record LoginRequest(String email, String password) {}
    public record StatusUpdateRequest(String status, String reason) {}
}

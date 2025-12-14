package com.ecoledger.application.controller;

import com.ecoledger.application.dto.*;
import com.ecoledger.application.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {

    private final UsuarioService service;

    public UsuariosController(UsuarioService service) {
        this.service = service;
    }

    @Operation(
            summary = "Submete novo cadastro de usuário",
            description = "Cria um novo pedido de cadastro para Produtores, Analistas ou Auditores.",
            tags = {"Cadastros"},
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastro criado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RespostaCadastroDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @PostMapping("/cadastros")
    public ResponseEntity<RespostaCadastroDto> createCadastro(
            @Parameter(description = "Chave idempotente para evitar duplicidade no cadastro.", required = false)
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CadastroCriacaoDto dto) {
        var resp = service.createCadastro(idempotencyKey, dto);
        return ResponseEntity.status(201).body(resp);
    }

    @Operation(
            summary = "Recupera pedido de cadastro",
            tags = {"Cadastros"},
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cadastro recuperado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CadastroDto.class))),
            @ApiResponse(responseCode = "404", description = "Cadastro não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @GetMapping("/cadastros/{id}")
    public ResponseEntity<CadastroDto> getCadastro(@PathVariable String id) {
        return ResponseEntity.ok(service.getCadastro(id));
    }

    @Operation(
            summary = "Autenticar usuário (email + senha)",
            tags = {"Autenticacao"},
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens gerados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenAuthDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @PostMapping("/auth/login")
    public ResponseEntity<TokenAuthDto> login(@Valid @RequestBody LoginRequest login) {
        return ResponseEntity.ok(service.authenticate(login.email(), login.password()));
    }

    @Operation(
            summary = "Obter perfil do usuário",
            tags = {"Usuarios"},
            security = {@SecurityRequirement(name = "bearerAuth", scopes = {"usuarios:read"})}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil recuperado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> getUsuario(@PathVariable String id) {
        return ResponseEntity.ok(service.getUsuario(id));
    }

    @Operation(
            summary = "Atualizar perfil do usuário (parcial)",
            tags = {"Usuarios"},
            security = {@SecurityRequirement(name = "bearerAuth", scopes = {"usuarios:write"})}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioDto> patchUsuario(@PathVariable String id, @Valid @RequestBody UsuarioAtualizacaoDto dto) {
        return ResponseEntity.ok(service.updateUsuario(id, dto));
    }

    @Operation(
            summary = "Atualiza status do usuário (admin/analista)",
            tags = {"Usuarios"},
            security = {@SecurityRequirement(name = "bearerAuth", scopes = {"admin:usuarios"})}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErroResponseDto.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioDto> patchStatus(@PathVariable String id, @Valid @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(service.updateStatus(id, req.status(), req.reason()));
    }

    // pequenos records internos para requests não descritos como componentes nos DTOs acima
    @Schema(name = "LoginRequest", description = "Credenciais necessárias para autenticação.")
    public record LoginRequest(
            @Schema(description = "Email do usuário.", format = "email", example = "produtor@ecoledger.com")
            @Email @NotBlank String email,
            @Schema(description = "Senha do usuário.", example = "SenhaFort3#")
            @NotBlank String password
    ) {
    }

    @Schema(name = "StatusUpdateRequest", description = "Payload para atualização do status do usuário.")
    public record StatusUpdateRequest(
            @Schema(description = "Novo status do usuário.", allowableValues = {"PENDENTE", "APROVADO", "REJEITADO"}, example = "APROVADO")
            @NotBlank String status,
            @Schema(description = "Motivo que levou à alteração do status.", example = "Documentos validados")
            String reason
    ) {
    }
}

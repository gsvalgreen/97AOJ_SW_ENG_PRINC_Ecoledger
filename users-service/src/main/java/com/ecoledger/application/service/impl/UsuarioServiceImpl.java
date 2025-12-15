package com.ecoledger.application.service.impl;

import com.ecoledger.application.dto.*;
import com.ecoledger.application.entity.*;
import com.ecoledger.application.service.IdempotencyService;
import com.ecoledger.events.EventPublisher;
import com.ecoledger.integration.NotificationClient;
import com.ecoledger.integration.security.JwtService;
import com.ecoledger.repository.*;
import com.ecoledger.application.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final CadastroRepository cadastroRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdempotencyService idempotencyService;
    private final EventPublisher eventPublisher;
    private final NotificationClient notificationClient;
    private final JwtService jwtService;

    public UsuarioServiceImpl(CadastroRepository cadastroRepository,
                              UsuarioRepository usuarioRepository,
                              IdempotencyService idempotencyService,
                              EventPublisher eventPublisher,
                              NotificationClient notificationClient,
                              JwtService jwtService) {
        this.cadastroRepository = cadastroRepository;
        this.usuarioRepository = usuarioRepository;
        this.idempotencyService = idempotencyService;
        this.eventPublisher = eventPublisher;
        this.notificationClient = notificationClient;
        this.jwtService = jwtService;
    }

    @Override
    public RespostaCadastroDto createCadastro(String idempotencyKey, CadastroCriacaoDto dto) {
        // idempotency: se já existe mapping, retorna mesmo cadastroId/status
        var existing = idempotencyService.findCadastroIdByKey(idempotencyKey);
        if (existing.isPresent()) {
            var c = cadastroRepository.findById(existing.get()).orElseThrow(() -> new IllegalArgumentException("Cadastro não encontrado"));
            return new RespostaCadastroDto(c.getId().toString(), c.getStatus());
        }

        UsuarioEntity user = new UsuarioEntity();
        user.setNome(dto.nome());
        user.setEmail(dto.email());
        user.setDocumento(dto.documento());
        user.setSenha(dto.senha());
        user.setRole(dto.role());
        user.setStatus("PENDENTE");

        CadastroEntity cadastro = new CadastroEntity();
        cadastro.setCandidatoUsuario(user);
        cadastro = cadastroRepository.save(cadastro);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.saveKey(idempotencyKey, cadastro.getId());
        }

        // publicar evento assíncrono e notificar
        var payload = new java.util.HashMap<String, Object>();
        payload.put("cadastroId", cadastro.getId());
        payload.put("status", cadastro.getStatus());
        payload.put("candidatoUsuario", MapUser(cadastro.getCandidatoUsuario()));
        payload.put("submetidoEm", cadastro.getSubmetidoEm());
        eventPublisher.publishRegistered(payload);
        notificationClient.notifyRegistration(payload);

        return new RespostaCadastroDto(cadastro.getId().toString(), cadastro.getStatus());
    }

    private java.util.Map<String, Object> MapUser(UsuarioEntity u) {
        var m = new java.util.HashMap<String, Object>();
        m.put("id", u.getId().toString());
        m.put("nome", u.getNome());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("documento", u.getDocumento());
        m.put("status", u.getStatus());
        m.put("criadoEm", u.getCriadoEm());
        return m;
    }

    @Override
    public CadastroDto getCadastro(String id) {
        return cadastroRepository.findById(UUID.fromString(id))
                .map(c -> new CadastroDto(
                        c.getId().toString(),
                        c.getStatus(),
                        new UsuarioDto(
                                c.getCandidatoUsuario().getId().toString(),
                                c.getCandidatoUsuario().getNome(),
                                c.getCandidatoUsuario().getEmail(),
                                c.getCandidatoUsuario().getRole(),
                                c.getCandidatoUsuario().getDocumento(),
                                c.getCandidatoUsuario().getStatus(),
                                c.getCandidatoUsuario().getCriadoEm()
                        ),
                        c.getSubmetidoEm()
                ))
                .orElseThrow(() -> new IllegalArgumentException("Cadastro não encontrado"));
    }

    @Override
    public TokenAuthDto authenticate(String email, String password) {
        Optional<UsuarioEntity> u = usuarioRepository.findByEmail(email);
        if (u.isEmpty()) throw new IllegalArgumentException("Credenciais inválidas");
        
        // Validar senha (comparação simples - em produção use BCrypt)
        if (!password.equals(u.get().getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        
        // Gerar tokens JWT reais
        String accessToken = jwtService.generateAccessToken(
            u.get().getId().toString(),
            u.get().getEmail(),
            u.get().getRole()
        );
        String refreshToken = jwtService.generateRefreshToken(u.get().getId().toString());
        Long expiresIn = jwtService.getExpirationInSeconds();
        
        return new TokenAuthDto(accessToken, refreshToken, expiresIn);
    }

    @Override
    public UsuarioDto getUsuario(String id) {
        return usuarioRepository.findById(UUID.fromString(id))
                .map(u -> new UsuarioDto(u.getId().toString(), u.getNome(), u.getEmail(), u.getRole(), u.getDocumento(), u.getStatus(), u.getCriadoEm()))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    @Override
    public UsuarioDto updateUsuario(String id, UsuarioAtualizacaoDto dto) {
        UsuarioEntity u = usuarioRepository.findById(UUID.fromString(id)).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (dto.nome() != null) u.setNome(dto.nome());
        usuarioRepository.save(u);
        return new UsuarioDto(u.getId().toString(), u.getNome(), u.getEmail(), u.getRole(), u.getDocumento(), u.getStatus(), u.getCriadoEm());
    }

    @Override
    public UsuarioDto updateStatus(String id, String status, String reason) {
        UsuarioEntity u = usuarioRepository.findById(UUID.fromString(id)).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        u.setStatus(status);
        usuarioRepository.save(u);

        var payload = new java.util.HashMap<String, Object>();
        payload.put("usuarioId", u.getId().toString());
        payload.put("status", status);
        payload.put("reason", reason);
        payload.put("timestamp", java.time.Instant.now());

        if ("APROVADO".equals(status)) {
            eventPublisher.publishApproved(payload);
            notificationClient.notifyApproval(payload);
        } else if ("REJEITADO".equals(status)) {
            eventPublisher.publishRejected(payload);
            notificationClient.notifyRejection(payload);
        }

        return new UsuarioDto(u.getId().toString(), u.getNome(), u.getEmail(), u.getRole(), u.getDocumento(), u.getStatus(), u.getCriadoEm());
    }
}

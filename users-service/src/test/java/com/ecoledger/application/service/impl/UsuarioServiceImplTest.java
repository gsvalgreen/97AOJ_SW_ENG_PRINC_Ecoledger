package com.ecoledger.application.service.impl;

import com.ecoledger.application.dto.*;
import com.ecoledger.application.entity.*;
import com.ecoledger.application.service.IdempotencyService;
import com.ecoledger.events.EventPublisher;
import com.ecoledger.integration.NotificationClient;
import com.ecoledger.integration.security.JwtService;
import com.ecoledger.repository.CadastroRepository;
import com.ecoledger.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    CadastroRepository cadastroRepository;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    IdempotencyService idempotencyService;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    NotificationClient notificationClient;
    @Mock
    JwtService jwtService;

    @InjectMocks
    UsuarioServiceImpl service;

    @Captor
    ArgumentCaptor<Object> anyPayload;

    UsuarioEntity sampleUsuario;
    CadastroEntity sampleCadastro;

    @BeforeEach
    void setup() {
        sampleUsuario = new UsuarioEntity();
        sampleUsuario.setNome("Joao");
        sampleUsuario.setEmail("j@d.com");
        sampleUsuario.setDocumento("doc1");
        sampleUsuario.setRole("ROLE_USER");
        sampleUsuario.setStatus("PENDENTE");

        sampleCadastro = new CadastroEntity();
        sampleCadastro.setCandidatoUsuario(sampleUsuario);
    }

    @Test
    void createCadastro_whenExistingIdempotency_returnsExisting() {
        var key = "k1";
        var cid = UUID.randomUUID();
        sampleCadastro = new CadastroEntity();
        sampleCadastro.setStatus("PENDENTE");
        sampleCadastro.setCandidatoUsuario(sampleUsuario);
        when(idempotencyService.findCadastroIdByKey(key)).thenReturn(Optional.of(cid));
        when(cadastroRepository.findById(cid)).thenReturn(Optional.of(sampleCadastro));

        var dto = new CadastroCriacaoDto("n","e","d","senha","r", java.util.Map.<String,Object>of(), List.<java.util.Map<String,Object>>of());
        var resp = service.createCadastro(key, dto);

        assertEquals(sampleCadastro.getStatus(), resp.status());
        verify(cadastroRepository).findById(cid);
        verify(eventPublisher, never()).publishRegistered(any());
    }

    @Test
    void createCadastro_whenNew_savesAndPublishes_and_savesIdempotency() {
        var key = "k2";
        when(idempotencyService.findCadastroIdByKey(key)).thenReturn(Optional.empty());
        when(cadastroRepository.save(any())).thenAnswer(i -> {
            CadastroEntity c = i.getArgument(0);
            // simulate id assigned
            try {
                var idField = CadastroEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(c, UUID.randomUUID());
            } catch (Exception ignored) {}
            return c;
        });

        var dto = new CadastroCriacaoDto("nome","email","doc","senha","role", java.util.Map.<String,Object>of(), List.<java.util.Map<String,Object>>of());
        var resp = service.createCadastro(key, dto);

        assertNotNull(resp.cadastroId());
        verify(idempotencyService).saveKey(eq(key), any());
        verify(eventPublisher).publishRegistered(any());
        verify(notificationClient).notifyRegistration(any());
    }

    @Test
    void getCadastro_whenFound_mapsCorrectly() {
        UUID id = UUID.randomUUID();
        sampleUsuario = new UsuarioEntity();
        try {
            var idField = UsuarioEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sampleUsuario, id);
        } catch (Exception ignored) {}
        sampleUsuario.setNome("N");
        sampleUsuario.setEmail("e");
        sampleUsuario.setRole("R");
        sampleUsuario.setDocumento("D");
        sampleUsuario.setStatus("S");
        sampleUsuario.setCriadoEm(Instant.now());

        sampleCadastro = new CadastroEntity();
        try {
            var idField = CadastroEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sampleCadastro, id);
        } catch (Exception ignored) {}
        sampleCadastro.setCandidatoUsuario(sampleUsuario);
        sampleCadastro.setStatus("PENDENTE");
        sampleCadastro.setSubmetidoEm(Instant.now());

        when(cadastroRepository.findById(id)).thenReturn(Optional.of(sampleCadastro));

        var dto = service.getCadastro(id.toString());
        assertEquals(id.toString(), dto.cadastroId());
        assertEquals("PENDENTE", dto.status());
        assertEquals("N", dto.candidatoUsuario().nome());
    }

    @Test
    void getCadastro_whenMissing_throws() {
        UUID id = UUID.randomUUID();
        when(cadastroRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getCadastro(id.toString()));
    }

    @Test
    void authenticate_success_and_failure() {
        var u = new UsuarioEntity();
        try { var f = UsuarioEntity.class.getDeclaredField("id"); f.setAccessible(true); f.set(u, UUID.randomUUID()); } catch (Exception ignored) {}
        u.setSenha("pw");
        u.setEmail("x@x");
        u.setRole("role");
        when(usuarioRepository.findByEmail("x@x" )).thenReturn(Optional.of(u));
        when(jwtService.generateAccessToken(anyString(), anyString(), anyString())).thenReturn("access.token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(100L);
        var t = service.authenticate("x@x", "pw");
        assertTrue(t.accessToken().startsWith("access."));

        when(usuarioRepository.findByEmail("no@" )).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.authenticate("no@", "pw"));
    }

    @Test
    void getUsuario_and_updateUsuario_and_notFound() {
        var id = UUID.randomUUID();
        var u = new UsuarioEntity();
        try { var f = UsuarioEntity.class.getDeclaredField("id"); f.setAccessible(true); f.set(u, id); } catch (Exception ignored) {}
        u.setNome("A"); u.setEmail("a@a"); u.setRole("r"); u.setDocumento("d"); u.setStatus("S"); u.setCriadoEm(Instant.now());
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(u));

        var dto = service.getUsuario(id.toString());
        assertEquals("A", dto.nome());

        var upd = new UsuarioAtualizacaoDto("B", java.util.Map.of());
        when(usuarioRepository.save(any())).thenReturn(u);
        var updated = service.updateUsuario(id.toString(), upd);
        assertEquals("B", updated.nome());

        UUID missing = UUID.randomUUID();
        when(usuarioRepository.findById(missing)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getUsuario(missing.toString()));
    }

    @Test
    void updateStatus_variousPaths() {
        var id = UUID.randomUUID();
        var u = new UsuarioEntity();
        try { var f = UsuarioEntity.class.getDeclaredField("id"); f.setAccessible(true); f.set(u, id); } catch (Exception ignored) {}
        u.setNome("A"); u.setEmail("a@a"); u.setRole("r"); u.setDocumento("d"); u.setStatus("PENDENTE"); u.setCriadoEm(Instant.now());
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(u));

        // aprovado
        var dto1 = service.updateStatus(id.toString(), "APROVADO", "ok");
        assertEquals("APROVADO", dto1.status());
        verify(eventPublisher).publishApproved(any());
        verify(notificationClient).notifyApproval(any());

        // rejeitado
        u.setStatus("PENDENTE");
        var dto2 = service.updateStatus(id.toString(), "REJEITADO", "bad");
        assertEquals("REJEITADO", dto2.status());
        verify(eventPublisher).publishRejected(any());
        verify(notificationClient).notifyRejection(any());

        // other status: no events
        u.setStatus("PENDENTE");
        var dto3 = service.updateStatus(id.toString(), "SOMETHING", "x");
        assertEquals("SOMETHING", dto3.status());
    }
}

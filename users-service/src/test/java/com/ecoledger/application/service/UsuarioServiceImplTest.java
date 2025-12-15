package com.ecoledger.application.service;

import com.ecoledger.application.dto.CadastroCriacaoDto;
import com.ecoledger.application.entity.UsuarioEntity;
import com.ecoledger.events.EventPublisher;
import com.ecoledger.integration.NotificationClient;
import com.ecoledger.repository.CadastroRepository;
import com.ecoledger.repository.UsuarioRepository;
import com.ecoledger.application.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @InjectMocks
    UsuarioServiceImpl service;

    @Test
    void createCadastro_whenNewIdempotencyKey_savesPublishesAndSavesKey() {
        var dto = new CadastroCriacaoDto(
                "Nome Teste",
                "teste@example.com",
                "12345678900",
                "senha123",
                "role",
                Map.<String,Object>of("fazenda", "x"),
                List.<Map<String,Object>>of(Map.<String,Object>of("anexo", "v"))
        );

        when(idempotencyService.findCadastroIdByKey("idem-key")).thenReturn(Optional.empty());
        when(cadastroRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resp = service.createCadastro("idem-key", dto);

        assertNotNull(resp);
        assertNotNull(resp.cadastroId());
        assertEquals("PENDENTE", resp.status());

        verify(cadastroRepository, times(1)).save(any());
        verify(idempotencyService, times(1)).saveKey(eq("idem-key"), any(java.util.UUID.class));
        verify(eventPublisher, times(1)).publishRegistered(any());
        verify(notificationClient, times(1)).notifyRegistration(any());
    }

    @Test
    void updateStatus_whenApproved_publishesApprovedAndNotifies() {
        var usuario = new UsuarioEntity();
        usuario.setNome("Usuario");
        usuario.setEmail("u@u.com");
        usuario.setDocumento("doc");
        usuario.setRole("role");
        usuario.setStatus("PENDENTE");

        when(usuarioRepository.findById(any(java.util.UUID.class))).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var id = UUID.randomUUID();
        var out = service.updateStatus(id.toString(), "APROVADO", "ok");

        assertNotNull(out);
        assertEquals("APROVADO", out.status());

        verify(eventPublisher, times(1)).publishApproved(any());
        verify(notificationClient, times(1)).notifyApproval(any());
    }

    @Test
    void updateStatus_whenRejected_publishesRejectedAndNotifies() {
        var usuario = new UsuarioEntity();
        usuario.setNome("Usuario");
        usuario.setEmail("u@u.com");
        usuario.setDocumento("doc");
        usuario.setRole("role");
        usuario.setStatus("PENDENTE");

        when(usuarioRepository.findById(any(java.util.UUID.class))).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var id = UUID.randomUUID();
        var out = service.updateStatus(id.toString(), "REJEITADO", "invalid");

        assertNotNull(out);
        assertEquals("REJEITADO", out.status());

        verify(eventPublisher, times(1)).publishRejected(any());
        verify(notificationClient, times(1)).notifyRejection(any());
    }
}


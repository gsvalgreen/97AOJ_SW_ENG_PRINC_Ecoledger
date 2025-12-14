package com.ecoledger.application.service;

import com.ecoledger.application.entity.IdempotencyEntity;
import com.ecoledger.repository.IdempotencyRepository;
import com.ecoledger.application.service.impl.IdempotencyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceImplTest {

    @Mock
    IdempotencyRepository repo;

    @InjectMocks
    IdempotencyServiceImpl service;

    @Test
    void findCadastroIdByKey_nullOrBlank_returnsEmpty() {
        assertTrue(service.findCadastroIdByKey(null).isEmpty());
        assertTrue(service.findCadastroIdByKey("").isEmpty());
        assertTrue(service.findCadastroIdByKey("   ").isEmpty());
    }

    @Test
    void findCadastroIdByKey_existingKey_returnsCadastroId() {
        var entity = new IdempotencyEntity("key-1", "cad-123");
        when(repo.findById("key-1")).thenReturn(Optional.of(entity));

        var out = service.findCadastroIdByKey("key-1");
        assertTrue(out.isPresent());
        assertEquals("cad-123", out.get());
    }

    @Test
    void saveKey_nullOrBlank_doesNotCallRepo() {
        service.saveKey(null, "x");
        service.saveKey("   ", "x");
        verify(repo, never()).save(any());
    }

    @Test
    void saveKey_valid_callsRepoSave() {
        service.saveKey("k1", "cad-1");
        ArgumentCaptor<IdempotencyEntity> cap = ArgumentCaptor.forClass(IdempotencyEntity.class);
        verify(repo, times(1)).save(cap.capture());
        assertEquals("k1", cap.getValue().getKey());
        assertEquals("cad-1", cap.getValue().getCadastroId());
    }
}

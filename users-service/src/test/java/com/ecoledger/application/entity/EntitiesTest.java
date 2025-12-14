package com.ecoledger.application.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EntitiesTest {

    @Test
    public void basic_entity_getters_setters() {
        UsuarioEntity u = new UsuarioEntity();
        Instant now = Instant.now();
        u.setCriadoEm(now);
        u.setStatus("ACTIVE");
        assertEquals(now, u.getCriadoEm());
        assertEquals("ACTIVE", u.getStatus());

        CadastroEntity c = new CadastroEntity();
        c.setStatus("APPROVED");
        c.setSubmetidoEm(now);
        assertEquals("APPROVED", c.getStatus());
        assertEquals(now, c.getSubmetidoEm());

        IdempotencyEntity id = new IdempotencyEntity("k1", UUID.randomUUID());
        assertNotNull(id.getCreatedAt());
    }
}

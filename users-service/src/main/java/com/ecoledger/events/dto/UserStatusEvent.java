package com.ecoledger.events.dto;

import java.time.Instant;

public record UserStatusEvent(
        String cadastroId,
        CandidateUser candidatoUsuario,
        String status,
        String reason,
        Instant timestamp
) { }

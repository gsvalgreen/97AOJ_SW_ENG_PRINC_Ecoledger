package com.ecoledger.events.dto;

import java.time.Instant;
import java.util.List;

public record UserRegisteredEvent(
        String cadastroId,
        CandidateUser candidatoUsuario,
        List<UserAttachment> anexos,
        Instant submetidoEm
) { }

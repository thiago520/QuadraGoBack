package com.quadrago.backend.dashboard.dto;

import java.time.Instant;

public record RecentActivityDto(
        String name,       // aluno/cliente
        String activity,   // "Nova matrícula", "Pagamento realizado", etc.
        Instant getHappenedAt // UTC
) {
}

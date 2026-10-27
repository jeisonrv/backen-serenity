package com.serenity.diario.application.port.in;

import com.serenity.diario.domain.model.EntradaDiario;

public interface GuardarEntradaDiarioUseCase {
    EntradaDiario guardar(Long usuarioId, String titulo, String contenido, String tipoPrompt);
}

package com.serenity.diario.application.port.in;

import com.serenity.diario.domain.model.EntradaDiario;

public interface GestionarEntradaDiarioUseCase {
    EntradaDiario obtener(Long idEntrada, Long usuarioId);
    EntradaDiario editar(Long idEntrada, Long usuarioId, String titulo, String contenido, String tipoPrompt);
    void eliminar(Long idEntrada, Long usuarioId);
}

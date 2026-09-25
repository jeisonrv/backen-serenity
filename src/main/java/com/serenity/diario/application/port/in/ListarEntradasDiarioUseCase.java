package com.serenity.diario.application.port.in;

import com.serenity.diario.domain.model.EntradaDiario;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ListarEntradasDiarioUseCase {
    List<EntradaDiario> listar(Long usuarioId);
    Page<EntradaDiario> listar(Long usuarioId, int page, int size);
}

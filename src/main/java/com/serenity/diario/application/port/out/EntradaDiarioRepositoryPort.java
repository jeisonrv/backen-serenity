package com.serenity.diario.application.port.out;

import com.serenity.diario.domain.model.EntradaDiario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface EntradaDiarioRepositoryPort {
    EntradaDiario guardar(EntradaDiario entrada);
    List<EntradaDiario> buscarPorUsuario(Long usuarioId);
    Page<EntradaDiario> buscarPaginaPorUsuario(Long usuarioId, int page, int size);
    Optional<EntradaDiario> buscarPropia(Long idEntrada, Long usuarioId);
    void eliminarPropia(Long idEntrada, Long usuarioId);
}

package com.serenity.usuario.application.port.in;

import com.serenity.usuario.domain.model.Rol;
import com.serenity.usuario.domain.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;

public interface GestionarAccesoUseCase {
    Rol verificarAcceso(Long id);
    boolean esOyenteActivo(Long id);
    Usuario restringir(Long id, String motivo, LocalDateTime hasta, Long moderadorId);
    boolean restringirSiAplica(Long id, String motivo);
    Usuario levantarRestriccion(Long id);
    List<Usuario> listarRestringidos();
}

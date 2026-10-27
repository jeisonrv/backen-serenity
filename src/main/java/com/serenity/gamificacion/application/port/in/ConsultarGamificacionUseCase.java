package com.serenity.gamificacion.application.port.in;
import com.serenity.gamificacion.domain.model.*;
import java.util.List;
public interface ConsultarGamificacionUseCase {
    List<RegistroJardin> jardin(Long usuarioId);
    List<Logro> logros(Long usuarioId);
    ResumenEstadisticas resumen(Long usuarioId);
}

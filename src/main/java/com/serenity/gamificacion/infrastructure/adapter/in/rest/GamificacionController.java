package com.serenity.gamificacion.infrastructure.adapter.in.rest;

import com.serenity.gamificacion.application.port.in.ConsultarGamificacionUseCase;
import com.serenity.gamificacion.domain.model.*;
import com.serenity.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class GamificacionController {
    private final ConsultarGamificacionUseCase consultar;
    public GamificacionController(ConsultarGamificacionUseCase consultar){this.consultar=consultar;}
    @GetMapping("/api/jardin")
    public List<RegistroJardin> jardin(@AuthenticationPrincipal AuthenticatedUser user){
        return consultar.jardin(user.id());
    }
    @GetMapping("/api/logros")
    public List<Logro> logros(@AuthenticationPrincipal AuthenticatedUser user){
        return consultar.logros(user.id());
    }
    @GetMapping("/api/estadisticas/resumen")
    public ResumenEstadisticas resumen(@AuthenticationPrincipal AuthenticatedUser user){
        return consultar.resumen(user.id());
    }
}

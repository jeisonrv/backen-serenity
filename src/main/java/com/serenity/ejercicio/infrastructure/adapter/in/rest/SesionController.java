package com.serenity.ejercicio.infrastructure.adapter.in.rest;

import com.serenity.ejercicio.application.port.in.CompletarSesionUseCase;
import com.serenity.ejercicio.domain.model.Sesion;
import com.serenity.ejercicio.infrastructure.adapter.in.rest.dto.CompletarSesionRequest;
import com.serenity.ejercicio.infrastructure.adapter.in.rest.dto.SesionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final CompletarSesionUseCase completarSesion;
    private final SesionRepositoryPort sesiones;

    @PostMapping
    public ResponseEntity<SesionResponse> completar(@RequestBody CompletarSesionRequest req,
                                                    @AuthenticationPrincipal AuthenticatedUser user) {
        if (req == null || req.duracion() == null) {
            throw new IllegalArgumentException("La duración es obligatoria");
        }
        Sesion sesion = completarSesion.completar(
                user.id(),
                req.tipoEjercicio(),
                req.duracion()
        );
        return ResponseEntity.ok(SesionResponse.desde(sesion));
    }

    @GetMapping
    public ResponseEntity<List<SesionResponse>> historial(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(sesiones.buscarPorUsuario(user.id()).stream().map(SesionResponse::desde).toList());
    }
}

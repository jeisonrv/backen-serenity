package com.serenity.ejercicio.infrastructure.adapter.in.rest;

import com.serenity.ejercicio.application.port.in.CompletarSesionUseCase;
import com.serenity.ejercicio.application.port.in.ListarSesionesUseCase;
import com.serenity.ejercicio.domain.model.Sesion;
import com.serenity.ejercicio.infrastructure.adapter.in.rest.dto.CompletarSesionRequest;
import com.serenity.ejercicio.infrastructure.adapter.in.rest.dto.SesionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.serenity.shared.security.AuthenticatedUser;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionController {

    private final CompletarSesionUseCase completarSesion;
    private final ListarSesionesUseCase listarSesiones;

    @PostMapping
    public ResponseEntity<SesionResponse> completar(@Valid @RequestBody CompletarSesionRequest req,
                                                    @AuthenticationPrincipal AuthenticatedUser user) {
        Sesion sesion = completarSesion.completar(
                user.id(),
                req.tipoEjercicio(),
                req.duracion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SesionResponse.desde(sesion));
    }

    @GetMapping
    public ResponseEntity<List<SesionResponse>> historial(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(listarSesiones.listar(user.id()).stream().map(SesionResponse::desde).toList());
    }
}

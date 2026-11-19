package com.serenity.oyente.infrastructure.adapter.in.rest;

import com.serenity.chat.application.port.in.ChatUseCase;
import com.serenity.chat.domain.model.Conversacion;
import com.serenity.oyente.application.port.in.GestionarDisponibilidadUseCase;
import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.shared.consent.RequireSensitiveConsent;
import com.serenity.shared.consent.ConsentService;
import com.serenity.shared.consent.SensitiveConsentRequiredException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/oyente")
public class OyenteController {

    public record EstadoResponse(
            boolean disponible,
            LocalDateTime fechaActualizacion
    ) {}

    public record CambiarEstadoRequest(
            @NotNull Boolean disponible
    ) {}

    public record CharlaDisponible(
            Long idConversacion,
            LocalDateTime fechaInicio,
            long minutosEnEspera
    ) {}

    public record CharlaDisponibleResponse(
            boolean hayCharlas,
            int total,
            List<CharlaDisponible> charlas
    ) {}

    public record CharlaActiva(
            Long idConversacion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {}

    public record AceptarResponse(
            Long idConversacion,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {}

    private final GestionarDisponibilidadUseCase disponibilidad;
    private final ChatUseCase chat;
    private final ConsentService consentService;

    public OyenteController(
            GestionarDisponibilidadUseCase disponibilidad,
            ChatUseCase chat,
            ConsentService consentService
    ) {
        this.disponibilidad = disponibilidad;
        this.chat = chat;
        this.consentService = consentService;
    }

    @GetMapping("/estado")
    public EstadoResponse estado(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var d = disponibilidad.obtener(user.id());

        return new EstadoResponse(
                d.disponible(),
                d.fechaActualizacion()
        );
    }

    @PutMapping("/estado")
    public EstadoResponse cambiarEstado(
            @Valid @RequestBody CambiarEstadoRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        var d = disponibilidad.cambiar(
                user.id(),
                request.disponible()
        );

        return new EstadoResponse(
                d.disponible(),
                d.fechaActualizacion()
        );
    }

    @GetMapping("/charlas/disponibles")
    @RequireSensitiveConsent
    public CharlaDisponibleResponse disponibles() {
        ZoneId zona = ZoneId.systemDefault();
        Instant ahora = Instant.now();

        List<CharlaDisponible> charlas = chat.listarEnEspera()
                .stream()
                .filter(c -> consentService.hasCurrentSensitiveConsent(c.idUsuario()))
                .map(c -> {
                    Instant inicio = c.fechaInicio()
                            .atZone(zona)
                            .toInstant();

                    long minutos = Math.max(
                            0,
                            ChronoUnit.MINUTES.between(inicio, ahora)
                    );

                    return new CharlaDisponible(
                            c.idConversacion(),
                            c.fechaInicio(),
                            minutos
                    );
                })
                .toList();

        return new CharlaDisponibleResponse(
                !charlas.isEmpty(),
                charlas.size(),
                charlas
        );
    }

    @GetMapping("/charlas/activas")
    @RequireSensitiveConsent
    public List<CharlaActiva> activas(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return chat.listarActivasDeOyente(user.id())
                .stream()
                .filter(c -> consentService.hasCurrentSensitiveConsent(c.idUsuario()))
                .map(c -> new CharlaActiva(
                        c.idConversacion(),
                        c.fechaInicio(),
                        c.fechaFin()
                ))
                .toList();
    }

    @PostMapping("/charlas/{id}/aceptar")
    @RequireSensitiveConsent
    public AceptarResponse aceptar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Conversacion pendiente = chat.buscarPorId(id);
        if (!consentService.hasCurrentSensitiveConsent(pendiente.idUsuario())) {
            throw new SensitiveConsentRequiredException();
        }
        Conversacion c = chat.aceptar(id, user.id());

        return new AceptarResponse(
                c.idConversacion(),
                c.fechaInicio(),
                c.fechaFin()
        );
    }
}

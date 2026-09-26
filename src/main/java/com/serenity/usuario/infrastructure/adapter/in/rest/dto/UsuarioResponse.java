package com.serenity.usuario.infrastructure.adapter.in.rest.dto;

import com.serenity.usuario.domain.model.Usuario;
import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String username, String email, int nivel, int xp,
                              Long idUsuario, String nombreUsuario, String correo,
                              int experiencia, int racha, int mejorRacha,
                              java.time.LocalDate ultimaActividad, LocalDateTime fechaRegistro) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getNivel(), u.getXp(),
                u.getId(), u.getUsername(), u.getEmail(), u.getXp(), u.getRacha(), u.getMejorRacha(),
                u.getUltimaActividad(), null);
    }
}

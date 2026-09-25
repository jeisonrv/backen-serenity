package com.serenity.usuario.infrastructure.adapter.in.rest;

import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioActualController {
    public record UsuarioActualResponse(Long idUsuario, String nombreUsuario, String correo, int nivel,
            int experiencia, int racha, int mejorRacha, LocalDate ultimaActividad, LocalDateTime fechaRegistro) {}
    private final UsuarioRepositoryPort usuarios;
    public UsuarioActualController(UsuarioRepositoryPort usuarios){this.usuarios=usuarios;}
    @GetMapping("/me")
    public UsuarioActualResponse actual(@AuthenticationPrincipal AuthenticatedUser user){
        var u=usuarios.buscarPorId(user.id()).orElseThrow(()->new IllegalStateException("Usuario no encontrado"));
        return new UsuarioActualResponse(u.getId(),u.getUsername(),u.getEmail(),u.getNivel(),u.getXp(),
                u.getRacha(),u.getMejorRacha(),u.getUltimaActividad(),null);
    }
}

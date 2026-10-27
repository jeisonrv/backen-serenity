package com.serenity.usuario.infrastructure.adapter.in.rest.dto;

import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/me/recordatorios")
public class RecordatoriosUsuarioController {

    public record PreferenciaRecordatoriosResponse(boolean activo) {}

    public record ActualizarPreferenciaRequest(boolean activo) {}

    private final UsuarioRepositoryPort usuarioRepository;

    public RecordatoriosUsuarioController(
            UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<PreferenciaRecordatoriosResponse> consultar(
            @AuthenticationPrincipal AuthenticatedUser user) {

        Usuario usuario = usuarioRepository.buscarPorId(user.id())
                .orElseThrow(() ->
                        new IllegalStateException("Usuario no encontrado"));

        return ResponseEntity.ok(
                new PreferenciaRecordatoriosResponse(
                        usuario.isRecordatoriosEmail()
                )
        );
    }

    @PutMapping
    public ResponseEntity<PreferenciaRecordatoriosResponse> actualizar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ActualizarPreferenciaRequest request) {

        Usuario usuario = usuarioRepository.buscarPorId(user.id())
                .orElseThrow(() ->
                        new IllegalStateException("Usuario no encontrado"));

        usuario.setRecordatoriosEmail(request.activo());

        Usuario guardado = usuarioRepository.guardar(usuario);

        return ResponseEntity.ok(
                new PreferenciaRecordatoriosResponse(
                        guardado.isRecordatoriosEmail()
                )
        );
    }
}

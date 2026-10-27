
package com.serenity.usuario.infrastructure.adapter.in.rest;

import com.serenity.shared.security.AuthenticatedUser;
import com.serenity.usuario.application.port.in.CambiarPasswordUseCase;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.in.RegistrarUsuarioUseCase;
import com.serenity.usuario.domain.model.Usuario;
import com.serenity.usuario.infrastructure.adapter.in.rest.dto.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    public record GoogleLoginRequest(
            @NotBlank
            @Size(max = 8192)
            String credential
    ) {}

    public record CambiarPasswordRequest(
            @NotBlank(message = "Debes escribir tu contraseña actual")
            String passwordActual,

            @NotBlank(message = "Debes escribir una contraseña nueva")
            @Size(
                    min = 8,
                    max = 72,
                    message = "La nueva contraseña debe tener entre 8 y 72 caracteres"
            )
            String passwordNueva
    ) {}

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final IniciarSesionUseCase iniciarSesion;
    private final CambiarPasswordUseCase cambiarPassword;

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuario,
            IniciarSesionUseCase iniciarSesion,
            CambiarPasswordUseCase cambiarPassword
    ) {
        this.registrarUsuario = registrarUsuario;
        this.iniciarSesion = iniciarSesion;
        this.cambiarPassword = cambiarPassword;
    }

    @PostMapping({"/registro", "/register"})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest req
    ) {
        Usuario usuario = registrarUsuario.registrar(
                req.username(),
                req.email(),
                req.password()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UsuarioResponse.desde(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest req
    ) {
        var resultado = iniciarSesion.iniciarSesion(
                req.email(),
                req.password()
        );

        return ResponseEntity.ok(
                new LoginResponse(
                        resultado.token(),
                        UsuarioResponse.desde(resultado.usuario())
                )
        );
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginConGoogle(
            @Valid @RequestBody GoogleLoginRequest req
    ) {
        var resultado = iniciarSesion.iniciarSesionConGoogle(
                req.credential()
        );

        return ResponseEntity.ok(
                new LoginResponse(
                        resultado.token(),
                        UsuarioResponse.desde(resultado.usuario())
                )
        );
    }

    @PostMapping("/google/vincular")
    public ResponseEntity<Void> vincularGoogle(
            @Valid @RequestBody GoogleLoginRequest req,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        iniciarSesion.vincularGoogle(
                user.id(),
                req.credential()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        cambiarPassword.cambiar(
                user.id(),
                request.passwordActual(),
                request.passwordNueva()
        );

        return ResponseEntity.noContent().build();
    }
}

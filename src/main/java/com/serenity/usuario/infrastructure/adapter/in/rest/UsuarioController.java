package com.serenity.usuario.infrastructure.adapter.in.rest;

import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.in.RegistrarUsuarioUseCase;
import com.serenity.usuario.domain.model.Usuario;
import com.serenity.usuario.infrastructure.adapter.in.rest.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final IniciarSesionUseCase iniciarSesion;

    public UsuarioController(RegistrarUsuarioUseCase registrarUsuario, IniciarSesionUseCase iniciarSesion) {
        this.registrarUsuario = registrarUsuario;
        this.iniciarSesion = iniciarSesion;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody RegistroRequest req) {
        Usuario usuario = registrarUsuario.registrar(req.username(), req.email(), req.password());
        return ResponseEntity.ok(UsuarioResponse.desde(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        var resultado = iniciarSesion.iniciarSesion(req.email(), req.password());
        return ResponseEntity.ok(new LoginResponse(resultado.token(), UsuarioResponse.desde(resultado.usuario())));
    }
}
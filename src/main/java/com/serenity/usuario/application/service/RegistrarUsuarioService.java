package com.serenity.usuario.application.service;

import com.serenity.usuario.application.port.in.RegistrarUsuarioUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import com.serenity.shared.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.serenity.notificacion.application.port.in.EnviarCorreoTransaccionalUseCase;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarUsuarioService.class);

    private final UsuarioRepositoryPort repositorio;
    private final PasswordEncoder passwordEncoder;
    private final EnviarCorreoTransaccionalUseCase correoTransaccional;

    public RegistrarUsuarioService(UsuarioRepositoryPort repositorio, PasswordEncoder passwordEncoder,
                                   EnviarCorreoTransaccionalUseCase correoTransaccional) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.correoTransaccional = correoTransaccional;
    }

    @Override
    public Usuario registrar(String username, String email, String passwordPlano) {
        try {
            Usuario usuario = registrarUsuario(username, email, passwordPlano);
            log.info("Registro de usuario completado para {}", enmascararCorreo(email));
            return usuario;
        } catch (RuntimeException ex) {
            log.warn("Fallo de registro para {}", enmascararCorreo(email));
            throw ex;
        }
    }

    private Usuario registrarUsuario(String username, String email, String passwordPlano) {
        if (username == null || username.isBlank() || email == null || email.isBlank() || passwordPlano == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        if (passwordPlano.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres");
        }
        if (repositorio.buscarPorEmail(email).isPresent()) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }

        Usuario usuario = repositorio.guardar(new Usuario(username, email, passwordEncoder.encode(passwordPlano)));
        correoTransaccional.enviarBienvenida(usuario);
        return usuario;
    }

    private String enmascararCorreo(String email) {
        if (email == null || email.isBlank()) {
            return "desconocido";
        }
        String normalizado = email.strip();
        int separador = normalizado.indexOf('@');
        return separador < 1 ? "***" : normalizado.charAt(0) + "***@***";
    }
}

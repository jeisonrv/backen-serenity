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
import com.serenity.shared.consent.ConsentService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarUsuarioService.class);

    private final UsuarioRepositoryPort repositorio;
    private final PasswordEncoder passwordEncoder;
    private final EnviarCorreoTransaccionalUseCase correoTransaccional;
    private final ConsentService consentimientos;

    public RegistrarUsuarioService(UsuarioRepositoryPort repositorio, PasswordEncoder passwordEncoder,
                                   EnviarCorreoTransaccionalUseCase correoTransaccional,
                                   ConsentService consentimientos) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.correoTransaccional = correoTransaccional;
        this.consentimientos = consentimientos;
    }

    @Override
    @Transactional
    public Usuario registrar(String username, String email, String passwordPlano,
                             Boolean acceptTerms, Boolean acceptSensitiveData) {
        try {
            Usuario usuario = registrarUsuario(username, email, passwordPlano,
                    acceptTerms, Boolean.TRUE.equals(acceptSensitiveData));
            log.info("Registro de usuario completado para {}", enmascararCorreo(email));
            return usuario;
        } catch (RuntimeException ex) {
            log.warn("Fallo de registro para {}", enmascararCorreo(email));
            throw ex;
        }
    }

    private Usuario registrarUsuario(String username, String email, String passwordPlano,
                                     Boolean acceptTerms, boolean acceptSensitiveData) {
        if (!Boolean.TRUE.equals(acceptTerms)) {
            throw new IllegalArgumentException(
                    "Debes aceptar los términos y confirmar que fuiste informado de la Política de Tratamiento de Datos"
            );
        }
        if (username == null || username.isBlank() || email == null || email.isBlank() || passwordPlano == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }
        String nombreUsuario = username.strip();
        int longitudNombre = nombreUsuario.codePointCount(0, nombreUsuario.length());
        if (longitudNombre < 3 || longitudNombre > 50) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres");
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

        Usuario usuarioNuevo = new Usuario(nombreUsuario, email, passwordEncoder.encode(passwordPlano));
        consentimientos.prepareRegistration(usuarioNuevo, acceptSensitiveData);
        Usuario usuario = repositorio.guardar(usuarioNuevo);
        consentimientos.recordRegistration(usuario.getId(), acceptSensitiveData);
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

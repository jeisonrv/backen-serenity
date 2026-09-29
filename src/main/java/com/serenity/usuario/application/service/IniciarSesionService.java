package com.serenity.usuario.application.service;

import com.serenity.shared.security.JwtUtil;
import com.serenity.shared.exception.CredencialesInvalidasException;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort repositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public IniciarSesionService(UsuarioRepositoryPort repositorio, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResultadoLogin iniciarSesion(String email, String passwordPlano) {
        Usuario usuario = repositorio.buscarPorEmail(email)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(passwordPlano, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtUtil.generarToken(usuario.getId(), usuario.getEmail());
        return new ResultadoLogin(usuario, token);
    }
}

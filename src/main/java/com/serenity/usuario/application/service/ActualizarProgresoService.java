package com.serenity.usuario.application.service;

import com.serenity.usuario.application.port.in.ActualizarProgresoUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActualizarProgresoService implements ActualizarProgresoUseCase {

    private final UsuarioRepositoryPort repositorio;

    public ActualizarProgresoService(UsuarioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public void actualizar(Long usuarioId, int xpGanado) {
        Usuario usuario = repositorio.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        usuario.agregarXP(xpGanado);
        usuario.registrarActividad(LocalDate.now());
        repositorio.guardar(usuario);
    }
}

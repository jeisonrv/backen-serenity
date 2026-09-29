package com.serenity.ejercicio.application.service;

import com.serenity.ejercicio.application.port.in.ListarSesionesUseCase;
import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import com.serenity.ejercicio.domain.model.Sesion;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListarSesionesService implements ListarSesionesUseCase {
    private final SesionRepositoryPort sesiones;

    public ListarSesionesService(SesionRepositoryPort sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public List<Sesion> listar(Long usuarioId) {
        return sesiones.buscarPorUsuario(usuarioId);
    }
}

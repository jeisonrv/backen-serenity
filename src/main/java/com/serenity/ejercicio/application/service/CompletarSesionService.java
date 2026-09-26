package com.serenity.ejercicio.application.service;

import com.serenity.ejercicio.application.port.in.CompletarSesionUseCase;
import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import com.serenity.ejercicio.domain.model.Sesion;
import com.serenity.usuario.application.port.in.ActualizarProgresoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompletarSesionService implements CompletarSesionUseCase {

    private final SesionRepositoryPort sesionRepo;
    private final ActualizarProgresoUseCase actualizarProgreso;

    public CompletarSesionService(SesionRepositoryPort sesionRepo, ActualizarProgresoUseCase actualizarProgreso) {
        this.sesionRepo = sesionRepo;
        this.actualizarProgreso = actualizarProgreso;
    }

    @Override
    @Transactional
    public Sesion completar(Long usuarioId, String tipoEjercicio, int duracionSegundos) {
        if (usuarioId == null || tipoEjercicio == null || tipoEjercicio.isBlank()
                || duracionSegundos <= 0) {
            throw new IllegalArgumentException("Tipo de ejercicio y duración positiva son obligatorios");
        }
        Sesion sesion = new Sesion(usuarioId, tipoEjercicio, duracionSegundos);
        Sesion guardada = sesionRepo.guardar(sesion);
        actualizarProgreso.actualizar(usuarioId, sesion.getXpGanado());
        return guardada;
    }
}

package com.serenity.animo.application.service;

import com.serenity.animo.application.port.in.ListarAnimoUseCase;
import com.serenity.animo.application.port.in.RegistrarAnimoUseCase;
import com.serenity.animo.application.port.out.EstadoAnimoRepositoryPort;
import com.serenity.animo.domain.model.EstadoAnimo;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class EstadoAnimoService implements RegistrarAnimoUseCase, ListarAnimoUseCase {
    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");
    private final EstadoAnimoRepositoryPort repository;
    public EstadoAnimoService(EstadoAnimoRepositoryPort repository) { this.repository = repository; }
    @Override public EstadoAnimo registrar(Long usuarioId, int valor) {
        if (valor < 1 || valor > 5) throw new IllegalArgumentException("El valor del ánimo debe estar entre 1 y 5");
        LocalDate hoy = LocalDate.now(ZONA_BOGOTA);
        LocalDateTime ahora = LocalDateTime.now(ZONA_BOGOTA);
        EstadoAnimo estadoHoy = repository.buscarPorUsuarioYDia(usuarioId, hoy)
                .map(actual -> new EstadoAnimo(actual.idAnimo(), usuarioId, valor, hoy, actual.fechaRegistro()))
                .orElseGet(() -> new EstadoAnimo(null, usuarioId, valor, hoy, ahora));
        return repository.guardar(estadoHoy);
    }
    @Override public List<EstadoAnimo> listar(Long usuarioId) { return repository.buscarPorUsuario(usuarioId); }
    @Override public Optional<EstadoAnimo> obtenerHoy(Long usuarioId) {
        return repository.buscarPorUsuarioYDia(usuarioId, LocalDate.now(ZONA_BOGOTA));
    }
    @Override public List<EstadoAnimo> historial(Long usuarioId, int dias) {
        if (dias < 1 || dias > 3650) throw new IllegalArgumentException("dias debe estar entre 1 y 3650");
        return repository.buscarHistorial(usuarioId, LocalDate.now(ZONA_BOGOTA).minusDays(dias - 1L));
    }
}

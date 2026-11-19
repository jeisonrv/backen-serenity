package com.serenity.gamificacion.application.service;

import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import com.serenity.ejercicio.domain.model.Sesion;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarGamificacionServiceTest {
    @Mock SesionRepositoryPort sesiones;
    @Mock UsuarioRepositoryPort usuarios;

    @Test void jardinMapeaSesionesDelUsuario() {
        when(sesiones.buscarPorUsuario(2L)).thenReturn(List.of(new Sesion(5L, 2L, "respirar", 120, 3, null)));
        var jardin = new ConsultarGamificacionService(sesiones, usuarios).jardin(2L);
        assertEquals(1, jardin.size());
        assertEquals("respirar", jardin.get(0).tipoPlanta());
    }

    @Test void resumenCalculaMinutosYRacha() {
        when(sesiones.buscarPorUsuario(2L)).thenReturn(List.of(new Sesion(2L, "respirar", 125), new Sesion(2L, "meditar", 60)));
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(new Usuario(2L, "Ana", "a@b.co", null, 1, 0, 4, 4)));
        var resumen = new ConsultarGamificacionService(sesiones, usuarios).resumen(2L);
        assertEquals(2, resumen.totalSesiones());
        assertEquals(3, resumen.minutosTotales());
        assertEquals(4, resumen.racha());
    }

    @Test void logrosSeDesbloqueanSegunCantidadDeSesiones() {
        when(sesiones.buscarPorUsuario(2L)).thenReturn(List.of(new Sesion(), new Sesion(), new Sesion(), new Sesion(), new Sesion()));
        var logros = new ConsultarGamificacionService(sesiones, usuarios).logros(2L);
        assertTrue(logros.get(0).desbloqueado());
        assertTrue(logros.get(1).desbloqueado());
        assertFalse(logros.get(2).desbloqueado());
    }
}

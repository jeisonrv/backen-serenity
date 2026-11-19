package com.serenity.animo.application.service;

import com.serenity.animo.application.port.out.EstadoAnimoRepositoryPort;
import com.serenity.animo.domain.model.EstadoAnimo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoAnimoServiceTest {

    private static final Long USUARIO_ID = 42L;
    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    @Mock
    private EstadoAnimoRepositoryPort repository;

    @InjectMocks
    private EstadoAnimoService service;

    @Test
    void registrarGuardaUnEstadoNuevoCuandoTodaviaNoExisteRegistroDeHoy() {
        LocalDate hoy = LocalDate.now(ZONA_COLOMBIA);
        when(repository.buscarPorUsuarioYDia(USUARIO_ID, hoy)).thenReturn(Optional.empty());
        when(repository.guardar(any(EstadoAnimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EstadoAnimo resultado = service.registrar(USUARIO_ID, 4);

        assertEquals(USUARIO_ID, resultado.idUsuario());
        assertEquals(4, resultado.valor());
        assertEquals(hoy, resultado.fechaDia());
        assertNotNull(resultado.fechaRegistro());
        verify(repository).guardar(any(EstadoAnimo.class));
    }

    @Test
    void registrarActualizaElRegistroExistenteYConservaSuIdentidadYFechaOriginal() {
        LocalDate hoy = LocalDate.now(ZONA_COLOMBIA);
        LocalDateTime fechaOriginal = LocalDateTime.now(ZONA_COLOMBIA).minusHours(2);
        EstadoAnimo existente = new EstadoAnimo(9L, USUARIO_ID, 2, hoy, fechaOriginal);

        when(repository.buscarPorUsuarioYDia(USUARIO_ID, hoy)).thenReturn(Optional.of(existente));
        when(repository.guardar(any(EstadoAnimo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EstadoAnimo resultado = service.registrar(USUARIO_ID, 5);

        assertEquals(9L, resultado.idAnimo());
        assertEquals(5, resultado.valor());
        assertEquals(fechaOriginal, resultado.fechaRegistro());
        assertEquals(hoy, resultado.fechaDia());
        verify(repository).guardar(any(EstadoAnimo.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 6, 10})
    void registrarRechazaValoresFueraDelRangoDeUnoACinco(int valor) {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(USUARIO_ID, valor));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 3651})
    void historialRechazaCantidadDeDiasFueraDelRangoPermitido(int dias) {
        assertThrows(IllegalArgumentException.class, () -> service.historial(USUARIO_ID, dias));
        verifyNoInteractions(repository);
    }
}

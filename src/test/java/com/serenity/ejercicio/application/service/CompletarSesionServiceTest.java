package com.serenity.ejercicio.application.service;

import com.serenity.ejercicio.application.port.out.SesionRepositoryPort;
import com.serenity.ejercicio.domain.model.Sesion;
import com.serenity.usuario.application.port.in.ActualizarProgresoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompletarSesionServiceTest {

    @Mock
    private SesionRepositoryPort sesionRepository;

    @Mock
    private ActualizarProgresoUseCase actualizarProgreso;

    @InjectMocks
    private CompletarSesionService service;

    @Test
    void completarGuardaSesionYActualizaLaExperienciaDelUsuario() {
        when(sesionRepository.guardar(any(Sesion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Sesion resultado = service.completar(7L, "respiracion", 300);

        assertNotNull(resultado);
        assertEquals(7L, resultado.getUsuarioId());
        assertEquals("respiracion", resultado.getTipoEjercicio());
        assertEquals(300, resultado.getDuracionSegundos());
        assertEquals(5, resultado.getXpGanado());
        verify(sesionRepository).guardar(any(Sesion.class));
        verify(actualizarProgreso).actualizar(7L, 5);
    }

    @ParameterizedTest
    @MethodSource("datosInvalidos")
    void completarRechazaDatosInvalidos(Long usuarioId, String tipo, int duracion) {
        assertThrows(IllegalArgumentException.class,
                () -> service.completar(usuarioId, tipo, duracion));
        verifyNoInteractions(sesionRepository, actualizarProgreso);
    }

    private static Stream<Arguments> datosInvalidos() {
        return Stream.of(
                Arguments.of(null, "respiracion", 60),
                Arguments.of(7L, null, 60),
                Arguments.of(7L, "   ", 60),
                Arguments.of(7L, "respiracion", 0),
                Arguments.of(7L, "respiracion", -1)
        );
    }
}

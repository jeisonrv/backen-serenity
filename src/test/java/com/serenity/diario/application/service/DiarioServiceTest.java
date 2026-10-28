package com.serenity.diario.application.service;

import com.serenity.diario.application.port.out.EntradaDiarioRepositoryPort;
import com.serenity.diario.domain.model.EntradaDiario;
import com.serenity.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiarioServiceTest {

    private static final Long USUARIO_ID = 19L;

    @Mock
    private EntradaDiarioRepositoryPort repository;

    @InjectMocks
    private DiarioService service;

    @Test
    void guardarGuardaContenidoYUsaPromptLibreCuandoNoSeIndicaUno() {
        when(repository.guardar(any(EntradaDiario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EntradaDiario resultado = service.guardar(USUARIO_ID, "Hoy me sentí más tranquila.", null);

        assertEquals(USUARIO_ID, resultado.idUsuario());
        assertEquals("Hoy me sentí más tranquila.", resultado.contenido());
        assertEquals("libre", resultado.tipoPrompt());
        assertNotNull(resultado.fechaCreacion());
        assertNotNull(resultado.fechaActualizacion());
        verify(repository).guardar(any(EntradaDiario.class));
    }

    @Test
    void guardarRechazaContenidoNuloOVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(USUARIO_ID, "   ", "libre"));
        verifyNoInteractions(repository);
    }

    @Test
    void guardarRechazaTitulosDeMasDeDoscientosCaracteres() {
        String tituloDemasiadoLargo = "T".repeat(201);

        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(USUARIO_ID, tituloDemasiadoLargo, "Contenido de prueba", "libre"));
        verifyNoInteractions(repository);
    }

    @Test
    void editarConservaTituloYPromptCuandoNoSeProporcionanNuevosValores() {
        LocalDateTime fechaCreacion = LocalDateTime.now().minusDays(1);
        EntradaDiario existente = new EntradaDiario(
                3L, USUARIO_ID, "Título anterior", "Texto anterior", "agradecimiento",
                fechaCreacion, fechaCreacion
        );
        when(repository.buscarPropia(3L, USUARIO_ID)).thenReturn(Optional.of(existente));
        when(repository.guardar(any(EntradaDiario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EntradaDiario resultado = service.editar(3L, USUARIO_ID, null, "Texto actualizado", null);

        assertEquals("Título anterior", resultado.titulo());
        assertEquals("Texto actualizado", resultado.contenido());
        assertEquals("agradecimiento", resultado.tipoPrompt());
        assertEquals(fechaCreacion, resultado.fechaCreacion());
        verify(repository).guardar(any(EntradaDiario.class));
    }

    @Test
    void obtenerFallaSiLaEntradaNoPerteneceAlUsuario() {
        when(repository.buscarPropia(50L, USUARIO_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtener(50L, USUARIO_ID));
        verify(repository).buscarPropia(50L, USUARIO_ID);
        verify(repository, never()).guardar(any(EntradaDiario.class));
    }
}

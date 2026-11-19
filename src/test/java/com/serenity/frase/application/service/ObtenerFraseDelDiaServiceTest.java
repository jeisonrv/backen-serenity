package com.serenity.frase.application.service;

import com.serenity.frase.application.port.out.ProveedorFrasesMotivacionalesPort;
import com.serenity.frase.domain.model.FraseMotivacional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObtenerFraseDelDiaServiceTest {
    @Mock ProveedorFrasesMotivacionalesPort proveedor;

    @Test void listaVaciaDevuelveFraseDeRespaldo() {
        when(proveedor.obtenerFrases()).thenReturn(List.of());
        var frase = new ObtenerFraseDelDiaService(proveedor).obtener();
        assertEquals("Serenity", frase.autor());
        assertFalse(frase.religiosa());
    }

    @Test void errorDelProveedorDevuelveFraseDeRespaldo() {
        when(proveedor.obtenerFrases()).thenThrow(new RuntimeException("sin servicio"));
        assertEquals("Serenity", new ObtenerFraseDelDiaService(proveedor).obtener().autor());
    }

    @Test void frasesSoloReligiosasDevuelvenRespaldo() {
        when(proveedor.obtenerFrases()).thenReturn(List.of(new FraseMotivacional("Fe", "A", true)));
        assertEquals("Serenity", new ObtenerFraseDelDiaService(proveedor).obtener().autor());
    }

    @Test void nuncaSeleccionaUnaFraseReligiosa() {
        when(proveedor.obtenerFrases()).thenReturn(List.of(
                new FraseMotivacional("A", "A", true), new FraseMotivacional("B", "B", false)));
        assertFalse(new ObtenerFraseDelDiaService(proveedor).obtener().religiosa());
    }
}

package com.serenity.frase.infrastructure.adapter.in.rest;

import com.serenity.frase.application.port.in.ObtenerFraseDelDiaUseCase;
import com.serenity.frase.domain.model.FraseMotivacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FraseMotivacionalControllerTest {
    @Mock ObtenerFraseDelDiaUseCase obtenerFrase;
    private MockMvc mvc;

    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new FraseMotivacionalController(obtenerFrase)).build();
    }

    @Test void getMotivacionalDevuelveFraseDelCasoDeUso() throws Exception {
        when(obtenerFrase.obtener()).thenReturn(new FraseMotivacional("Sigue adelante", "Serenity", false));

        mvc.perform(get("/api/frases/motivacional"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.frase").value("Sigue adelante"))
                .andExpect(jsonPath("$.autor").value("Serenity"))
                .andExpect(jsonPath("$.religiosa").value(false));

        verify(obtenerFrase).obtener();
    }

    @Test void soloPublicaElMetodoGet() throws Exception {
        mvc.perform(post("/api/frases/motivacional"))
                .andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(obtenerFrase);
    }
}

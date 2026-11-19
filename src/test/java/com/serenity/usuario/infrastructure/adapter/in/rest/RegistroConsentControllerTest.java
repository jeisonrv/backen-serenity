package com.serenity.usuario.infrastructure.adapter.in.rest;

import com.serenity.shared.exception.GlobalExceptionHandler;
import com.serenity.usuario.application.port.in.CambiarPasswordUseCase;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.in.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegistroConsentControllerTest {

    @Test
    void rejectsRegistrationWhenTermsFieldIsMissingWithClearBadRequest() throws Exception {
        RegistrarUsuarioUseCase registrar = mock(RegistrarUsuarioUseCase.class);
        when(registrar.registrar(anyString(), anyString(), anyString(), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException(
                        "Debes aceptar los términos y confirmar que fuiste informado de la Política de Tratamiento de Datos"));
        UsuarioController controller = new UsuarioController(
                registrar, mock(IniciarSesionUseCase.class), mock(CambiarPasswordUseCase.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content("""
                                {"username":"ana","email":"ana@example.com","password":"secreto"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "Debes aceptar los términos y confirmar que fuiste informado de la Política de Tratamiento de Datos"));

        verify(registrar).registrar("ana", "ana@example.com", "secreto", null, null);
    }
}

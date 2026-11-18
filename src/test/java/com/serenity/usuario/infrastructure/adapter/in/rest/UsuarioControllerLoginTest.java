package com.serenity.usuario.infrastructure.adapter.in.rest;

import com.serenity.shared.config.SecurityConfig;
import com.serenity.shared.consent.ConsentService;
import com.serenity.shared.security.JwtUtil;
import com.serenity.usuario.application.port.in.CambiarPasswordUseCase;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.in.RegistrarUsuarioUseCase;
import com.serenity.usuario.domain.model.Rol;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UsuarioController.class,
        properties = {
                "jwt.secret=controller-login-test-key-with-at-least-32-bytes",
                "jwt.expiration=60000"
        }
)
@Import({SecurityConfig.class, JwtUtil.class})
@ImportAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
class UsuarioControllerLoginTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RegistrarUsuarioUseCase registrarUsuario;

    @MockBean
    private IniciarSesionUseCase iniciarSesion;

    @MockBean
    private CambiarPasswordUseCase cambiarPassword;

    @MockBean
    private GestionarAccesoUseCase gestionarAcceso;

    @MockBean
    private ConsentService consentService;

    @Test
    void loginRespondeConTokenYDatosDeUsuarioSinIncluirElHash() throws Exception {
        Usuario oyente = new Usuario(
                31L, "serenity.oyente", "oyente.serenity@example.com", "$2a$hash-privado", 1, 0, 0, 0);
        oyente.setRol(Rol.OYENTE);
        when(iniciarSesion.iniciarSesion("oyente.serenity@example.com", "Oyente123#"))
                .thenReturn(new IniciarSesionUseCase.ResultadoLogin(oyente, "jwt-firmado"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"oyente.serenity@example.com","password":"Oyente123#"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-firmado"))
                .andExpect(jsonPath("$.usuario.id").value(31))
                .andExpect(jsonPath("$.usuario.email").value("oyente.serenity@example.com"))
                .andExpect(jsonPath("$.usuario.username").value("serenity.oyente"))
                .andExpect(jsonPath("$.usuario.rol").value("OYENTE"))
                .andExpect(jsonPath("$.usuario.idUsuario").value(31))
                .andExpect(jsonPath("$.usuario.nombreUsuario").value("serenity.oyente"))
                .andExpect(jsonPath("$.usuario.correo").value("oyente.serenity@example.com"))
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
    }
}

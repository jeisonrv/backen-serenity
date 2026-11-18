package com.serenity.usuario.infrastructure.adapter.in.rest;

import com.serenity.shared.config.SecurityConfig;
import com.serenity.shared.consent.ConsentService;
import com.serenity.shared.exception.ConflictException;
import com.serenity.shared.security.JwtUtil;
import com.serenity.usuario.application.port.in.CambiarPasswordUseCase;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.application.port.in.IniciarSesionUseCase;
import com.serenity.usuario.application.port.in.ObtenerUsuarioActualUseCase;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {UsuarioController.class, UsuarioActualController.class},
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
    private ObtenerUsuarioActualUseCase obtenerUsuarioActual;

    @MockBean
    private GestionarAccesoUseCase gestionarAcceso;

    @MockBean
    private ConsentService consentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void registroIncluyeGoogleVinculadoSinExponerPassword() throws Exception {
        Usuario usuario = new Usuario(32L, "nueva", "nueva@example.com", "hash", 1, 0, 0, 0);
        when(registrarUsuario.registrar("nueva", "nueva@example.com", "password-segura", true, false))
                .thenReturn(usuario);

        mvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nueva","email":"nueva@example.com",
                                 "password":"password-segura","acceptTerms":true,
                                 "acceptSensitiveData":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.googleVinculado").value(false))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginRespondeConTokenYDatosDeUsuarioSinIncluirElHash() throws Exception {
        Usuario oyente = new Usuario(
                31L, "serenity.oyente", "oyente.serenity@example.com", "$2a$hash-privado", 1, 0, 0, 0);
        oyente.setRol(Rol.OYENTE);
        oyente.setGoogleSubject("google-subject");
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
                .andExpect(jsonPath("$.usuario.googleVinculado").value(true))
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
    }

    @Test
    void loginGoogleIncluyeGoogleVinculado() throws Exception {
        Usuario usuario = new Usuario(33L, "google", "google@example.com", null, 1, 0, 0, 0);
        usuario.setGoogleSubject("google-subject");
        when(iniciarSesion.iniciarSesionConGoogle(anyString(), any(), any(), any()))
                .thenReturn(new IniciarSesionUseCase.ResultadoLogin(usuario, "jwt-google"));

        mvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"credential":"google-id-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.googleVinculado").value(true))
                .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist());
    }

    @Test
    void vincularGoogleAceptaLaRutaCortaConSesionSerenity() throws Exception {
        when(gestionarAcceso.verificarAcceso(34L)).thenReturn(Rol.USUARIO);
        String jwt = jwtUtil.generarToken(34L, "perfil@example.com", Rol.USUARIO);

        mvc.perform(post("/api/auth/vincular")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"credential":"google-id-token"}
                                """))
                .andExpect(status().isNoContent());

        verify(iniciarSesion).vincularGoogle(34L, "google-id-token");
    }

    @Test
    void vincularGoogleNoAceptaIdTokenGoogleComoBearerDeSerenity() throws Exception {
        mvc.perform(post("/api/auth/vincular")
                        .header("Authorization", "Bearer google-id-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"credential":"google-id-token"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(iniciarSesion, never()).vincularGoogle(anyLong(), anyString());
    }

    @Test
    void perfilActualSinSesionDevuelve401() throws Exception {
        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilActualIncluyeVinculoYExistenciaDePasswordSinExponerHash() throws Exception {
        Usuario usuario = new Usuario(34L, "perfil", "perfil@example.com", null, 2, 120, 3, 5);
        usuario.setGoogleSubject("google-subject");
        when(gestionarAcceso.verificarAcceso(34L)).thenReturn(Rol.USUARIO);
        when(obtenerUsuarioActual.obtener(34L)).thenReturn(usuario);
        String jwt = jwtUtil.generarToken(34L, "perfil@example.com", Rol.USUARIO);

        mvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(34))
                .andExpect(jsonPath("$.nombreUsuario").value("perfil"))
                .andExpect(jsonPath("$.correo").value("perfil@example.com"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.googleVinculado").value(true))
                .andExpect(jsonPath("$.tienePassword").value(false))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void perfilActualIndicaCuandoLaCuentaTienePassword() throws Exception {
        Usuario usuario = new Usuario(35L, "mixta", "mixta@example.com", "bcrypt-hash", 1, 0, 0, 0);
        when(gestionarAcceso.verificarAcceso(35L)).thenReturn(Rol.USUARIO);
        when(obtenerUsuarioActual.obtener(35L)).thenReturn(usuario);
        String jwt = jwtUtil.generarToken(35L, "mixta@example.com", Rol.USUARIO);

        mvc.perform(get("/api/usuarios/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleVinculado").value(false))
                .andExpect(jsonPath("$.tienePassword").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void correoExistenteSinVinculoDevuelveCodigoDeConflictoEstable() throws Exception {
        when(iniciarSesion.iniciarSesionConGoogle(anyString(), any(), any(), any()))
                .thenThrow(new ConflictException(
                        "Ya existe una cuenta con este correo.", "GOOGLE_ACCOUNT_NOT_LINKED"));

        mvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"credential":"google-id-token"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("GOOGLE_ACCOUNT_NOT_LINKED"));
    }
}

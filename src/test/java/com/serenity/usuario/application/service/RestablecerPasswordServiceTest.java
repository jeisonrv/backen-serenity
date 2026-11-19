package com.serenity.usuario.application.service;

import com.serenity.usuario.application.port.out.TokenRestablecimientoPasswordPort;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestablecerPasswordServiceTest {
    @Mock TokenRestablecimientoPasswordPort tokens;
    @Mock UsuarioRepositoryPort usuarios;
    @Mock PasswordEncoder encoder;

    @Test void tokenInvalidoProduceSolicitudIncorrecta() {
        var servicio = new RestablecerPasswordService(tokens, usuarios, encoder);
        var error = assertThrows(ResponseStatusException.class, () -> servicio.restablecer("token", "nuevaClave"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test void restableceClaveYMarcaTokenComoUsado() {
        String token = "token-seguro";
        String hash = SolicitarRestablecimientoPasswordService.sha256(token);
        Usuario usuario = new Usuario(9L, "Ana", "ana@b.co", "antigua", 1, 0, 0, 0);
        when(tokens.buscarValidoPorHash(eq(hash), any())).thenReturn(Optional.of(
                new TokenRestablecimientoPasswordPort.TokenRestablecimiento(hash, 9L, Instant.now().plusSeconds(60))));
        when(usuarios.buscarPorId(9L)).thenReturn(Optional.of(usuario));
        when(encoder.encode("nuevaClave123")).thenReturn("$2nueva");
        var servicio = new RestablecerPasswordService(tokens, usuarios, encoder);
        servicio.restablecer(token, "nuevaClave123");
        assertEquals("$2nueva", usuario.getPasswordHash());
        verify(usuarios).guardar(usuario);
        verify(tokens).marcarUsado(eq(hash), any());
    }
}

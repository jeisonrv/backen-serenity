package com.serenity.usuario.application.service;

import com.serenity.notificacion.application.port.out.ProveedorCorreoPort;
import com.serenity.usuario.application.port.out.TokenRestablecimientoPasswordPort;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitarRestablecimientoPasswordServiceTest {
    @Mock UsuarioRepositoryPort usuarios;
    @Mock TokenRestablecimientoPasswordPort tokens;
    @Mock ProveedorCorreoPort correo;

    @Test void guardaHashDelTokenYEnviaEnlaceSiLaCuentaPuedeRecuperarse() {
        Usuario usuario = new Usuario(7L, "Ana", "ana@b.co", "$2hash", 1, 0, 0, 0);
        when(usuarios.buscarPorEmailIgnorandoMayusculas("ana@b.co")).thenReturn(Optional.of(usuario));
        var servicio = new SolicitarRestablecimientoPasswordService(usuarios, tokens, correo, true, "https://serenity.test/");
        servicio.solicitar(" ana@b.co ");
        verify(tokens).guardar(argThat(hash -> hash.matches("[a-f0-9]{64}")), eq(7L), any());
        verify(correo).enviar(eq("ana@b.co"), contains("contraseña"), contains("restablecer-password?token="), contains("restablecer-password?token="));
    }

    @Test void cuentaInexistenteNoGuardaTokenNiEnviaCorreo() {
        when(usuarios.buscarPorEmailIgnorandoMayusculas("no@b.co")).thenReturn(Optional.empty());
        new SolicitarRestablecimientoPasswordService(usuarios, tokens, correo, true, "https://serenity.test")
                .solicitar("no@b.co");
        verifyNoInteractions(tokens, correo);
    }
}

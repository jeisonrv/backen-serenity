package com.serenity.shared.config;

import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Rol;
import com.serenity.usuario.domain.model.Usuario;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    private static final String OYENTE_PASSWORD = "Oyente123#";
    private static final String MODERADOR_PASSWORD = "Mod123#";

    @Mock
    private UsuarioRepositoryPort usuarios;

    @Test
    void creaLasCuentasUnaSolaVezConHashBcryptYRolesInstitucionales() {
        Map<String, Usuario> cuentas = new HashMap<>();
        configurarRepositorioEnMemoria(cuentas);
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        DataSeeder seeder = seeder(bcrypt);

        seeder.run(new DefaultApplicationArguments());
        seeder.run(new DefaultApplicationArguments());

        assertEquals(2, cuentas.size());
        Usuario oyente = cuentas.get("oyente.serenity@example.com");
        Usuario moderador = cuentas.get("moderador.serenity@example.com");
        assertNotNull(oyente);
        assertNotNull(moderador);
        assertEquals(Rol.OYENTE, oyente.getRol());
        assertEquals(Rol.MODERADOR, moderador.getRol());
        assertNotEquals("Oyente123#", oyente.getPasswordHash());
        assertNotEquals("Mod123#", moderador.getPasswordHash());
        assertTrue(bcrypt.matches(OYENTE_PASSWORD, oyente.getPasswordHash()));
        assertTrue(bcrypt.matches(MODERADOR_PASSWORD, moderador.getPasswordHash()));
        verify(usuarios, times(2)).guardarSiNoExiste(any(Usuario.class));
    }

    @Test
    void existenteConservaHashYNoSeElevaDeRolSinVerificarLaContrasena() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String hashExistente = bcrypt.encode("clave-anterior");
        Usuario existente = new Usuario(
                7L, "usuario", "oyente.serenity@example.com", hashExistente, 3, 25, 2, 4);
        when(usuarios.buscarPorEmailIgnorandoMayusculas("oyente.serenity@example.com"))
                .thenReturn(Optional.of(existente));
        when(usuarios.buscarPorEmailIgnorandoMayusculas("moderador.serenity@example.com"))
                .thenReturn(Optional.empty());
        when(usuarios.guardarSiNoExiste(any(Usuario.class))).thenReturn(true);
        DataSeeder seeder = seeder(bcrypt);

        seeder.run(new DefaultApplicationArguments());

        assertEquals(Rol.USUARIO, existente.getRol());
        assertEquals(hashExistente, existente.getPasswordHash());
        assertEquals("usuario", existente.getUsername());
        assertEquals(3, existente.getNivel());
        assertEquals(25, existente.getXp());
        assertTrue(bcrypt.matches("clave-anterior", existente.getPasswordHash()));
        verify(usuarios, never()).guardar(existente);
    }

    @Test
    void existenteContrasenaVerificadaPuedeRecibirSuRolSinCambiarSuHash() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String hashExistente = bcrypt.encode(OYENTE_PASSWORD);
        Usuario existente = new Usuario(
                7L, "usuario", "oyente.serenity@example.com", hashExistente, 3, 25, 2, 4);
        when(usuarios.buscarPorEmailIgnorandoMayusculas("oyente.serenity@example.com"))
                .thenReturn(Optional.of(existente));
        when(usuarios.buscarPorEmailIgnorandoMayusculas("moderador.serenity@example.com"))
                .thenReturn(Optional.empty());
        when(usuarios.guardarSiNoExiste(any(Usuario.class))).thenReturn(true);
        when(usuarios.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        seeder(bcrypt).run(new DefaultApplicationArguments());

        assertEquals(Rol.OYENTE, existente.getRol());
        assertEquals(hashExistente, existente.getPasswordHash());
        assertTrue(bcrypt.matches(OYENTE_PASSWORD, existente.getPasswordHash()));
        verify(usuarios).guardar(existente);
    }

    private DataSeeder seeder(BCryptPasswordEncoder bcrypt) {
        return new DataSeeder(usuarios, bcrypt, OYENTE_PASSWORD, MODERADOR_PASSWORD);
    }

    private void configurarRepositorioEnMemoria(Map<String, Usuario> cuentas) {
        when(usuarios.buscarPorEmailIgnorandoMayusculas(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return Optional.ofNullable(cuentas.get(email.toLowerCase(Locale.ROOT)));
        });
        when(usuarios.guardarSiNoExiste(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            return cuentas.putIfAbsent(
                    usuario.getEmail().toLowerCase(Locale.ROOT), usuario) == null;
        });
    }
}

package com.serenity.usuario.application.service;

import com.serenity.shared.exception.AccesoRestringidoException;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.application.port.in.GestionarAccesoUseCase;
import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Rol;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class GestionarAccesoService implements GestionarAccesoUseCase {
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");
    private final UsuarioRepositoryPort usuarios;
    public GestionarAccesoService(UsuarioRepositoryPort usuarios) { this.usuarios = usuarios; }

    @Override @Transactional(readOnly = true)
    public Rol verificarAcceso(Long id) {
        Usuario u = buscar(id);
        if (u.estaRestringido(ahora())) throw new AccesoRestringidoException(u.getRestringidoHasta());
        return u.getRol();
    }
    @Override @Transactional(readOnly = true)
    public boolean esOyenteActivo(Long id) {
        return usuarios.buscarPorId(id).map(u -> u.getRol() == Rol.OYENTE && !u.estaRestringido(ahora())).orElse(false);
    }
    @Override
    public Usuario restringir(Long id, String motivo, LocalDateTime hasta, Long moderadorId) {
        Usuario u = buscar(id);
        if (u.getRol() == Rol.MODERADOR) throw new IllegalArgumentException("No se puede restringir a un moderador");
        u.restringir(motivo, hasta, ahora(), moderadorId);
        return usuarios.guardar(u);
    }
    @Override
    public boolean restringirSiAplica(Long id, String motivo) {
        Usuario u = usuarios.buscarPorId(id).orElse(null);
        if (u == null || u.getRol() == Rol.MODERADOR || u.estaRestringido(ahora())) return false;
        u.restringir(motivo, null, ahora(), null);
        usuarios.guardar(u);
        return true;
    }
    @Override
    public Usuario levantarRestriccion(Long id) {
        Usuario u = buscar(id); u.levantarRestriccion(); return usuarios.guardar(u);
    }
    @Override @Transactional(readOnly = true)
    public List<Usuario> listarRestringidos() {
        LocalDateTime ahora = ahora();
        return usuarios.listarRestringidos().stream().filter(u -> u.estaRestringido(ahora)).toList();
    }
    private Usuario buscar(Long id) { return usuarios.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")); }
    private LocalDateTime ahora() { return LocalDateTime.now(ZONA_HORARIA); }
}

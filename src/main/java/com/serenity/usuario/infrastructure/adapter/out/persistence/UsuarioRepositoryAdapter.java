package com.serenity.usuario.infrastructure.adapter.out.persistence;

import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity entity = toEntity(usuario);
        UsuarioJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmailIgnorandoMayusculas(String email) {
        return jpaRepository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorGoogleSubject(String googleSubject) {
        return jpaRepository.findByGoogleSubject(googleSubject).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Usuario> listarRestringidos() {
        return jpaRepository.findByRestringidoTrueOrderByFechaRestriccionDesc().stream().map(this::toDomain).toList();
    }

    private UsuarioJpaEntity toEntity(Usuario u) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity(u.getId(), u.getUsername(), u.getEmail(), u.getPasswordHash(),
                u.getNivel(), u.getXp(), u.getRacha(), u.getMejorRacha());
        entity.setUltimaActividad(u.getUltimaActividad());
        entity.setRol(u.getRol());
        entity.setRestringido(u.isRestringido());
        entity.setMotivoRestriccion(u.getMotivoRestriccion());
        entity.setRestringidoHasta(u.getRestringidoHasta());
        entity.setFechaRestriccion(u.getFechaRestriccion());
        entity.setRestringidoPor(u.getRestringidoPor());
        entity.setGoogleSubject(u.getGoogleSubject());
        return entity;
    }

    private Usuario toDomain(UsuarioJpaEntity e) {
        Usuario usuario = new Usuario(e.getId(), e.getUsername(), e.getEmail(), e.getPasswordHash(),
                e.getNivel(), e.getXp(), e.getRacha(), e.getMejorRacha());
        usuario.setUltimaActividad(e.getUltimaActividad());
        usuario.setRol(e.getRol());
        usuario.restaurarRestriccion(e.isRestringido(), e.getMotivoRestriccion(), e.getRestringidoHasta(), e.getFechaRestriccion(), e.getRestringidoPor());
        usuario.setGoogleSubject(e.getGoogleSubject());
        return usuario;
    }
}

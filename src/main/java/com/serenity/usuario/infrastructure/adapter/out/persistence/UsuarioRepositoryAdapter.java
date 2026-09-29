package com.serenity.usuario.infrastructure.adapter.out.persistence;

import com.serenity.usuario.application.port.out.UsuarioRepositoryPort;
import com.serenity.usuario.domain.model.Usuario;
import org.springframework.stereotype.Component;
import java.util.Optional;

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
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private UsuarioJpaEntity toEntity(Usuario u) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity(u.getId(), u.getUsername(), u.getEmail(), u.getPasswordHash(),
                u.getNivel(), u.getXp(), u.getRacha(), u.getMejorRacha());
        entity.setUltimaActividad(u.getUltimaActividad());
        return entity;
    }

    private Usuario toDomain(UsuarioJpaEntity e) {
        Usuario usuario = new Usuario(e.getId(), e.getUsername(), e.getEmail(), e.getPasswordHash(),
                e.getNivel(), e.getXp(), e.getRacha(), e.getMejorRacha());
        usuario.setUltimaActividad(e.getUltimaActividad());
        return usuario;
    }
}

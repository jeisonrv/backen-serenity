package com.serenity.usuario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByEmail(String email);
    Optional<UsuarioJpaEntity> findByEmailIgnoreCase(String email);
    @Modifying
    @Query(value = """
            INSERT INTO usuarios
                (username, email, password_hash, nivel, xp, racha, mejor_racha,
                 rol, restringido, recordatorios_email)
            VALUES
                (:username, :email, :passwordHash, :nivel, :xp, :racha, :mejorRacha,
                 :rol, false, false)
            ON CONFLICT (email) DO NOTHING
            """, nativeQuery = true)
    int insertarSiEmailNoExiste(
            @Param("username") String username,
            @Param("email") String email,
            @Param("passwordHash") String passwordHash,
            @Param("nivel") int nivel,
            @Param("xp") int xp,
            @Param("racha") int racha,
            @Param("mejorRacha") int mejorRacha,
            @Param("rol") String rol);
    Optional<UsuarioJpaEntity> findByGoogleSubject(String googleSubject);
    List<UsuarioJpaEntity> findByRestringidoTrueOrderByFechaRestriccionDesc();
    List<UsuarioJpaEntity> findByRecordatoriosEmailTrue();
}

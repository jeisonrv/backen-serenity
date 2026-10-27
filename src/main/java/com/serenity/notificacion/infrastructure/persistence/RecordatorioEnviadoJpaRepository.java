package com.serenity.notificacion.infrastructure.persistence;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordatorioEnviadoJpaRepository
        extends JpaRepository<RecordatorioEnviadoJpaEntity, Long> {

    boolean existsByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
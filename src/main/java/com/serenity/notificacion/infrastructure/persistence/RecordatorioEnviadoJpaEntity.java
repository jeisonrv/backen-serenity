package com.serenity.notificacion.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "recordatorios_enviados",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recordatorio_usuario_fecha",
                columnNames = {"usuario_id", "fecha"}
        )
)
public class RecordatorioEnviadoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    protected RecordatorioEnviadoJpaEntity() {
    }

    public RecordatorioEnviadoJpaEntity(
            Long usuarioId,
            LocalDate fecha,
            LocalDateTime fechaEnvio) {
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.fechaEnvio = fechaEnvio;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
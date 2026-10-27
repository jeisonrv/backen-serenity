package com.serenity.ejercicio.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones")
public class SesionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;
    private String tipoEjercicio;
    private int duracionSegundos;
    private int xpGanado;
    private LocalDateTime fechaCompletado;

    public SesionJpaEntity() {}

    public SesionJpaEntity(Long id, Long usuarioId, String tipoEjercicio, int duracionSegundos, int xpGanado) {
        this(id, usuarioId, tipoEjercicio, duracionSegundos, xpGanado, LocalDateTime.now());
    }

    public SesionJpaEntity(Long id, Long usuarioId, String tipoEjercicio, int duracionSegundos, int xpGanado,
                           LocalDateTime fechaCompletado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipoEjercicio = tipoEjercicio;
        this.duracionSegundos = duracionSegundos;
        this.xpGanado = xpGanado;
        this.fechaCompletado = fechaCompletado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTipoEjercicio() { return tipoEjercicio; }
    public void setTipoEjercicio(String tipoEjercicio) { this.tipoEjercicio = tipoEjercicio; }
    public int getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(int duracionSegundos) { this.duracionSegundos = duracionSegundos; }
    public int getXpGanado() { return xpGanado; }
    public void setXpGanado(int xpGanado) { this.xpGanado = xpGanado; }
    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }
}

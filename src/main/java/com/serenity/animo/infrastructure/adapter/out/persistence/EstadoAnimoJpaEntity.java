package com.serenity.animo.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "estados_animo", uniqueConstraints = @UniqueConstraint(name = "uk_animo_usuario_dia", columnNames = {"id_usuario", "fecha_dia"}))
public class EstadoAnimoJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long idAnimo;
    @Column(nullable = false) private Long idUsuario;
    @Column(nullable = false) private int valor;
    @Column(name = "fecha_dia") private LocalDate fechaDia;
    @Column(nullable = false) private LocalDateTime fechaRegistro;
    protected EstadoAnimoJpaEntity() {}
    public EstadoAnimoJpaEntity(Long idAnimo, Long idUsuario, int valor, LocalDate fechaDia, LocalDateTime fechaRegistro) {
        this.idAnimo=idAnimo; this.idUsuario=idUsuario; this.valor=valor; this.fechaDia=fechaDia; this.fechaRegistro=fechaRegistro;
    }
    public Long getIdAnimo(){return idAnimo;} public Long getIdUsuario(){return idUsuario;}
    public int getValor(){return valor;} public LocalDate getFechaDia(){return fechaDia;} public LocalDateTime getFechaRegistro(){return fechaRegistro;}
}

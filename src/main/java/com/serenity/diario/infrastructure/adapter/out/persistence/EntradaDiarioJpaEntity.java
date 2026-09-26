package com.serenity.diario.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="entradas_diario")
public class EntradaDiarioJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long idEntrada;
    @Column(nullable=false) private Long idUsuario;
    @Column(nullable=false,columnDefinition="text") private String contenido;
    @Column(nullable=false) private String tipoPrompt;
    @Column(name="fecha_registro",nullable=false) private LocalDateTime fechaCreacion;
    @Column(name="fecha_actualizacion") private LocalDateTime fechaActualizacion;
    protected EntradaDiarioJpaEntity(){}
    public EntradaDiarioJpaEntity(Long id,Long usuario,String titulo,String contenido,String tipo,LocalDateTime creada,LocalDateTime actualizada){idEntrada=id;idUsuario=usuario;this.titulo=titulo;this.contenido=contenido;tipoPrompt=tipo;fechaCreacion=creada;fechaActualizacion=actualizada;}
    @PrePersist void inicializarFechas(){if(fechaCreacion==null)fechaCreacion=LocalDateTime.now();if(fechaActualizacion==null)fechaActualizacion=fechaCreacion;}
    @Column(length=200) private String titulo;
    public Long getIdEntrada(){return idEntrada;} public Long getIdUsuario(){return idUsuario;} public String getTitulo(){return titulo;} public String getContenido(){return contenido;} public String getTipoPrompt(){return tipoPrompt;} public LocalDateTime getFechaCreacion(){return fechaCreacion;} public LocalDateTime getFechaActualizacion(){return fechaActualizacion;} public void setTitulo(String titulo){this.titulo=titulo;} public void setContenido(String contenido){this.contenido=contenido;} public void setTipoPrompt(String tipoPrompt){this.tipoPrompt=tipoPrompt;} public void setFechaActualizacion(LocalDateTime fecha){fechaActualizacion=fecha;}
}

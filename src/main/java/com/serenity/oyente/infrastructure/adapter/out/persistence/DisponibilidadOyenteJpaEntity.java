package com.serenity.oyente.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="oyentes_disponibilidad")
public class DisponibilidadOyenteJpaEntity {
 @Id private Long idOyente;
 @Column(nullable=false) private boolean disponible;
 @Column(nullable=false) private LocalDateTime fechaActualizacion;
 protected DisponibilidadOyenteJpaEntity(){}
 public DisponibilidadOyenteJpaEntity(Long id,boolean disponible,LocalDateTime fecha){this.idOyente=id;this.disponible=disponible;this.fechaActualizacion=fecha;}
 public Long getIdOyente(){return idOyente;} public boolean isDisponible(){return disponible;} public LocalDateTime getFechaActualizacion(){return fechaActualizacion;}
}

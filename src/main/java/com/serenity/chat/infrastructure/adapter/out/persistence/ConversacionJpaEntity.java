package com.serenity.chat.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="conversaciones")
public class ConversacionJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long idConversacion;
 @Column(nullable=false) private Long idUsuario;
 @Column(nullable=false) private Long idOyente;
 @Column(nullable=false) private LocalDateTime fechaInicio;
 private LocalDateTime fechaFin;
 protected ConversacionJpaEntity(){}
 public ConversacionJpaEntity(Long id,Long u,Long o,LocalDateTime inicio,LocalDateTime fin){idConversacion=id;idUsuario=u;idOyente=o;fechaInicio=inicio;fechaFin=fin;}
 public Long getIdConversacion(){return idConversacion;} public Long getIdUsuario(){return idUsuario;} public Long getIdOyente(){return idOyente;} public LocalDateTime getFechaInicio(){return fechaInicio;} public LocalDateTime getFechaFin(){return fechaFin;} public void setFechaFin(LocalDateTime f){fechaFin=f;}
}

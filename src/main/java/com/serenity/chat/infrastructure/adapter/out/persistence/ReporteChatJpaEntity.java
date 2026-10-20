package com.serenity.chat.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.serenity.chat.domain.model.EstadoReporte;
import org.hibernate.annotations.ColumnDefault;
@Entity @Table(name="reportes_chat")
public class ReporteChatJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long idConversacion;
 @Column(nullable=false) private Long idUsuario;
 @Column(nullable=false,length=1000) private String motivo;
 @Column(nullable=false) private LocalDateTime fechaReporte;
 private Long idReportado;
 @Enumerated(EnumType.STRING) @Column(nullable=false) @ColumnDefault("'PENDIENTE'") private EstadoReporte estado=EstadoReporte.PENDIENTE;
 private Long idModerador;
 @Column(length=1000) private String observacionModerador;
 private LocalDateTime fechaResolucion;
 protected ReporteChatJpaEntity(){}
 public ReporteChatJpaEntity(Long c,Long u,String m){idConversacion=c;idUsuario=u;motivo=m;fechaReporte=LocalDateTime.now();}
 public ReporteChatJpaEntity(Long id,Long conversacion,Long reportante,Long reportado,String motivo,EstadoReporte estado,LocalDateTime fecha,Long moderador,String observacion,LocalDateTime resolucion){this.id=id;this.idConversacion=conversacion;this.idUsuario=reportante;this.idReportado=reportado;this.motivo=motivo;this.estado=estado;this.fechaReporte=fecha;this.idModerador=moderador;this.observacionModerador=observacion;this.fechaResolucion=resolucion;}
 public Long getId(){return id;} public Long getIdConversacion(){return idConversacion;} public Long getIdUsuario(){return idUsuario;} public Long getIdReportado(){return idReportado;} public String getMotivo(){return motivo;} public EstadoReporte getEstado(){return estado;} public LocalDateTime getFechaReporte(){return fechaReporte;} public Long getIdModerador(){return idModerador;} public String getObservacionModerador(){return observacionModerador;} public LocalDateTime getFechaResolucion(){return fechaResolucion;}
}

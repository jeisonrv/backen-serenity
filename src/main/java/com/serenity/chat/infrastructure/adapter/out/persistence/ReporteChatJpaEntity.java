package com.serenity.chat.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="reportes_chat")
public class ReporteChatJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long idConversacion;
 @Column(nullable=false) private Long idUsuario;
 @Column(nullable=false,length=1000) private String motivo;
 @Column(nullable=false) private LocalDateTime fechaReporte;
 protected ReporteChatJpaEntity(){}
 public ReporteChatJpaEntity(Long c,Long u,String m){idConversacion=c;idUsuario=u;motivo=m;fechaReporte=LocalDateTime.now();}
}

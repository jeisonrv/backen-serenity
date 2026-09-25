package com.serenity.chat.infrastructure.adapter.out.persistence;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="mensajes_chat")
public class MensajeChatJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long idConversacion;
 @Column(nullable=false,length=20) private String remitente;
 @Column(nullable=false,columnDefinition="text") private String contenido;
 @Column(nullable=false) private LocalDateTime fechaEnvio;
 protected MensajeChatJpaEntity(){}
 public MensajeChatJpaEntity(Long c,String r,String t){idConversacion=c;remitente=r;contenido=t;fechaEnvio=LocalDateTime.now();}
}

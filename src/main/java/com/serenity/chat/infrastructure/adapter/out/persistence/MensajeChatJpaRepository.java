package com.serenity.chat.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MensajeChatJpaRepository extends JpaRepository<MensajeChatJpaEntity,Long>{List<MensajeChatJpaEntity> findByIdConversacionOrderByFechaEnvioAscIdAsc(Long conversacion);}

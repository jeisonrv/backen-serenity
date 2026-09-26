package com.serenity.chat.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ConversacionJpaRepository extends JpaRepository<ConversacionJpaEntity,Long>{Optional<ConversacionJpaEntity> findByIdConversacionAndIdUsuario(Long id,Long usuarioId);}

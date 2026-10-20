package com.serenity.chat.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
public interface ConversacionJpaRepository extends JpaRepository<ConversacionJpaEntity,Long>{
 @Query("select c from ConversacionJpaEntity c where c.idConversacion=:id and (c.idUsuario=:u or c.idOyente=:u)") Optional<ConversacionJpaEntity> buscarParticipante(@Param("id") Long id,@Param("u") Long usuarioId);
 List<ConversacionJpaEntity> findByIdOyenteIsNullAndFechaFinIsNullOrderByFechaInicioAsc();
 List<ConversacionJpaEntity> findByIdOyenteAndFechaFinIsNullOrderByFechaInicioAsc(Long oyente);
 long countByIdOyenteAndFechaFinIsNull(Long oyente);
 @Modifying(clearAutomatically=true,flushAutomatically=true)
 @Query("update ConversacionJpaEntity c set c.idOyente=:oyente where c.idConversacion=:id and c.idOyente is null and c.fechaFin is null and c.idUsuario <> :oyente") int asignarOyente(@Param("id") Long id,@Param("oyente") Long oyente);
}

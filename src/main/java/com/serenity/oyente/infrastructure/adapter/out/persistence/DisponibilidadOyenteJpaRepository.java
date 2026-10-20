package com.serenity.oyente.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DisponibilidadOyenteJpaRepository extends JpaRepository<DisponibilidadOyenteJpaEntity,Long> {
 List<DisponibilidadOyenteJpaEntity> findByDisponibleTrueOrderByFechaActualizacionAsc();
}

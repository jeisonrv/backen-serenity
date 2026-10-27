package com.serenity.diario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EntradaDiarioJpaRepository extends JpaRepository<EntradaDiarioJpaEntity,Long>{
    List<EntradaDiarioJpaEntity> findByIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);
    Page<EntradaDiarioJpaEntity> findByIdUsuarioOrderByFechaCreacionDesc(Long idUsuario,Pageable pageable);
    Optional<EntradaDiarioJpaEntity> findByIdEntradaAndIdUsuario(Long idEntrada,Long idUsuario);
}

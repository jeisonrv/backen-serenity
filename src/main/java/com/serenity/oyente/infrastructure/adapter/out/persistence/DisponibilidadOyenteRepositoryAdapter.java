package com.serenity.oyente.infrastructure.adapter.out.persistence;
import com.serenity.oyente.application.port.out.DisponibilidadOyenteRepositoryPort;
import com.serenity.oyente.domain.model.DisponibilidadOyente;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
@Component public class DisponibilidadOyenteRepositoryAdapter implements DisponibilidadOyenteRepositoryPort {
 private final DisponibilidadOyenteJpaRepository repository;
 public DisponibilidadOyenteRepositoryAdapter(DisponibilidadOyenteJpaRepository repository){this.repository=repository;}
 @Override public Optional<DisponibilidadOyente> buscar(Long id){return repository.findById(id).map(this::dominio);}
 @Override public DisponibilidadOyente guardar(DisponibilidadOyente d){return dominio(repository.save(new DisponibilidadOyenteJpaEntity(d.idOyente(),d.disponible(),d.fechaActualizacion())));}
 @Override public List<DisponibilidadOyente> listarDisponibles(){return repository.findByDisponibleTrueOrderByFechaActualizacionAsc().stream().map(this::dominio).toList();}
 private DisponibilidadOyente dominio(DisponibilidadOyenteJpaEntity e){return new DisponibilidadOyente(e.getIdOyente(),e.isDisponible(),e.getFechaActualizacion());}
}

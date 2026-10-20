package com.serenity.oyente.application.service;
import com.serenity.oyente.application.port.in.GestionarDisponibilidadUseCase;
import com.serenity.oyente.application.port.out.DisponibilidadOyenteRepositoryPort;
import com.serenity.oyente.domain.model.DisponibilidadOyente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service @Transactional
public class GestionarDisponibilidadService implements GestionarDisponibilidadUseCase {
 private final DisponibilidadOyenteRepositoryPort repository;
 public GestionarDisponibilidadService(DisponibilidadOyenteRepositoryPort repository){this.repository=repository;}
 @Override @Transactional(readOnly=true) public DisponibilidadOyente obtener(Long id){return repository.buscar(id).orElse(new DisponibilidadOyente(id,false,null));}
 @Override public DisponibilidadOyente cambiar(Long id,boolean disponible){return repository.guardar(new DisponibilidadOyente(id,disponible,LocalDateTime.now()));}
 @Override @Transactional(readOnly=true) public boolean estaDisponible(Long id){return repository.buscar(id).map(DisponibilidadOyente::disponible).orElse(false);}
 @Override @Transactional(readOnly=true) public List<Long> listarDisponibles(){return repository.listarDisponibles().stream().map(DisponibilidadOyente::idOyente).toList();}
}

package com.serenity.animo.infrastructure.adapter.out.persistence;

import com.serenity.animo.application.port.out.EstadoAnimoRepositoryPort;
import com.serenity.animo.domain.model.EstadoAnimo;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Component
public class EstadoAnimoRepositoryAdapter implements EstadoAnimoRepositoryPort {
    private final EstadoAnimoJpaRepository repository;
    public EstadoAnimoRepositoryAdapter(EstadoAnimoJpaRepository repository){this.repository=repository;}
    @Override public EstadoAnimo guardar(EstadoAnimo e){return dominio(repository.save(new EstadoAnimoJpaEntity(e.idAnimo(),e.idUsuario(),e.valor(),e.fechaDia(),e.fechaRegistro())));}
    @Override public List<EstadoAnimo> buscarPorUsuario(Long id){return repository.findByIdUsuarioOrderByFechaRegistroDesc(id).stream().map(this::dominio).toList();}
    @Override public Optional<EstadoAnimo> buscarPorUsuarioYDia(Long id,LocalDate dia){
        Optional<EstadoAnimoJpaEntity> encontrado=repository.findByIdUsuarioAndFechaDia(id,dia);
        if(encontrado.isEmpty())encontrado=repository.findByIdUsuarioOrderByFechaRegistroDesc(id).stream()
                .filter(e->e.getFechaDia()==null&&e.getFechaRegistro().toLocalDate().equals(dia)).findFirst();
        return encontrado.map(this::dominio);
    }
    @Override public List<EstadoAnimo> buscarHistorial(Long id,LocalDate desde){
        java.util.Map<LocalDate,EstadoAnimo> porDia=new java.util.LinkedHashMap<>();
        repository.findByIdUsuarioOrderByFechaRegistroDesc(id).stream().map(this::dominio)
                .filter(e->!e.fechaDia().isBefore(desde)).forEach(e->porDia.putIfAbsent(e.fechaDia(),e));
        return List.copyOf(porDia.values());
    }
    private EstadoAnimo dominio(EstadoAnimoJpaEntity e){return new EstadoAnimo(e.getIdAnimo(),e.getIdUsuario(),e.getValor(),e.getFechaDia()!=null?e.getFechaDia():e.getFechaRegistro().toLocalDate(),e.getFechaRegistro());}
}

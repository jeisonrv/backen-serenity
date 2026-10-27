package com.serenity.oyente.application.port.out;
import com.serenity.oyente.domain.model.DisponibilidadOyente;
import java.util.List;
import java.util.Optional;
public interface DisponibilidadOyenteRepositoryPort {
 Optional<DisponibilidadOyente> buscar(Long id);
 DisponibilidadOyente guardar(DisponibilidadOyente disponibilidad);
 List<DisponibilidadOyente> listarDisponibles();
}

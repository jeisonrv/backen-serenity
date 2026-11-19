package com.serenity.frase.application.service;

import com.serenity.frase.application.port.in.ObtenerFraseDelDiaUseCase;
import com.serenity.frase.application.port.out.ProveedorFrasesMotivacionalesPort;
import com.serenity.frase.domain.model.FraseMotivacional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ObtenerFraseDelDiaService
        implements ObtenerFraseDelDiaUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ObtenerFraseDelDiaService.class);

    private static final ZoneId ZONA_COLOMBIA =
            ZoneId.of("America/Bogota");

    private final ProveedorFrasesMotivacionalesPort proveedor;

    public ObtenerFraseDelDiaService(
            ProveedorFrasesMotivacionalesPort proveedor
    ) {
        this.proveedor = proveedor;
    }

    @Override
    public FraseMotivacional obtener() {

        try {
            List<FraseMotivacional> disponibles =
                    proveedor.obtenerFrases().stream()
                            .filter(frase -> !frase.religiosa())
                            .toList();

            if (disponibles.isEmpty()) {
                return fraseRespaldo();
            }

            int indice = Math.floorMod(
                    LocalDate.now(ZONA_COLOMBIA).getDayOfYear(),
                    disponibles.size()
            );

            return disponibles.get(indice);

        } catch (RuntimeException e) {
            log.warn(
                    "No se pudo consultar la API de frases motivacionales: {}",
                    e.getMessage()
            );

            return fraseRespaldo();
        }
    }

    private FraseMotivacional fraseRespaldo() {
        return new FraseMotivacional(
                "Pequeños pasos también cuentan.",
                "Serenity",
                false
        );
    }
}
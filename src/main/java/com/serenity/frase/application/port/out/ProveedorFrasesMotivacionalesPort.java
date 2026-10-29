package com.serenity.frase.application.port.out;

import com.serenity.frase.domain.model.FraseMotivacional;

import java.util.List;

public interface ProveedorFrasesMotivacionalesPort {

    List<FraseMotivacional> obtenerFrases();

}
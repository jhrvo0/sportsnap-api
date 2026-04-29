package com.sportsnap.session.domain.usecases;

public interface RegistrarAtividade {

    void executar(Long checkInId, Double distancia, Integer duracaoSegundos, String intensidade);
}

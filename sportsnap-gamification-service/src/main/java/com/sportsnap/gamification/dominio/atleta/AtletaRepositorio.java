package com.sportsnap.gamification.dominio.atleta;

import java.util.List;
import java.util.Optional;

public interface AtletaRepositorio {

    Atleta salvar(Atleta atleta);

    Optional<Atleta> obter(AtletaId id);

    List<Atleta> listarTodos();

    void limpar();
}

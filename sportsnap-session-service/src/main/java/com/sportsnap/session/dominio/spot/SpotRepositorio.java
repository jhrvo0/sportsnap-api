package com.sportsnap.session.dominio.spot;

import java.util.List;
import java.util.Optional;

public interface SpotRepositorio {

    Spot salvar(Spot spot);

    Optional<Spot> obter(SpotId id);

    List<Spot> listarTodos();

    void limpar();
}

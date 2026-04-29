package com.sportsnap.marketplace.dominio.fotografo;

import java.util.List;
import java.util.Optional;

public interface FotografoRepositorio {

    Fotografo salvar(Fotografo fotografo);

    Optional<Fotografo> obter(FotografoId id);

    List<Fotografo> listarTodos();

    void limpar();
}

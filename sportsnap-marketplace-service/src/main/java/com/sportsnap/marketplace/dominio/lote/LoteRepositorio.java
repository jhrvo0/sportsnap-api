package com.sportsnap.marketplace.dominio.lote;

import com.sportsnap.marketplace.dominio.fotografo.FotografoId;

import java.util.List;
import java.util.Optional;

public interface LoteRepositorio {

    Lote salvar(Lote lote);

    Optional<Lote> obter(LoteId id);

    List<Lote> listarPorFotografo(FotografoId fotografoId);

    void limpar();
}

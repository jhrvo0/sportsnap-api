package com.sportsnap.marketplace.dominio.foto;

import com.sportsnap.marketplace.dominio.lote.LoteId;

import java.util.List;
import java.util.Optional;

public interface FotoRepositorio {

    Foto salvar(Foto foto);

    Optional<Foto> obter(FotoId id);

    List<Foto> listarPorLote(LoteId loteId);

    List<Foto> listarTodas();

    void limpar();
}

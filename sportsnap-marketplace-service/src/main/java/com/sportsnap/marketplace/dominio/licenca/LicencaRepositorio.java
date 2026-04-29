package com.sportsnap.marketplace.dominio.licenca;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;

import java.util.List;
import java.util.Optional;

public interface LicencaRepositorio {

    LicencaDeImagem salvar(LicencaDeImagem licenca);

    Optional<LicencaDeImagem> obter(LicencaId id);

    List<LicencaDeImagem> listarPorAtleta(AtletaId atletaId);

    List<LicencaDeImagem> listarPorFoto(FotoId fotoId);

    List<LicencaDeImagem> listarTodas();

    void limpar();
}

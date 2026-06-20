package com.sportsnap.marketplace.dominio.assinatura;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import java.util.Optional;

public interface AssinaturaRepositorio {

    Assinatura salvar(Assinatura assinatura);

    Optional<Assinatura> obterPorAtleta(AtletaId atletaId);

    void limpar();
}

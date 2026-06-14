package com.sportsnap.marketplace.dominio.licenca;

import java.util.List;
import java.util.Optional;

public interface SplitRepositorio {

    SplitFinanceiro salvar(SplitFinanceiro split);

    Optional<SplitFinanceiro> obterPorLicenca(LicencaId licencaId);

    List<SplitFinanceiro> listarTodos();

    void limpar();
}

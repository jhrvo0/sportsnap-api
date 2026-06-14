package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.licenca.LicencaId;
import com.sportsnap.marketplace.dominio.licenca.SplitFinanceiro;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// @Repository (desativado: usando JPA)
public class SplitRepositorioMemoria implements SplitRepositorio {

    private final Map<Integer, SplitFinanceiro> armazem = new ConcurrentHashMap<>();

    @Override
    public SplitFinanceiro salvar(SplitFinanceiro split) {
        armazem.put(split.getLicencaId().getId(), split);
        return split;
    }

    @Override
    public Optional<SplitFinanceiro> obterPorLicenca(LicencaId licencaId) {
        if (licencaId == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(licencaId.getId()));
    }

    @Override
    public List<SplitFinanceiro> listarTodos() {
        return new ArrayList<>(armazem.values());
    }

    @Override
    public void limpar() {
        armazem.clear();
    }
}

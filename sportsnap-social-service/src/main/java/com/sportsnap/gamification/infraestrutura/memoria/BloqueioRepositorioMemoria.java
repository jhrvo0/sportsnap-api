package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.bloqueio.Bloqueio;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioId;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BloqueioRepositorioMemoria implements BloqueioRepositorio {

    private final Map<BloqueioId, Bloqueio> dados = new ConcurrentHashMap<>();

    @Override
    public Bloqueio salvar(Bloqueio bloqueio) {
        dados.put(bloqueio.getId(), bloqueio);
        return bloqueio;
    }

    @Override
    public void remover(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        dados.values().removeIf(b ->
            b.getBloqueadorId().equals(bloqueadorId) && b.getBloqueadoId().equals(bloqueadoId));
    }

    @Override
    public Optional<Bloqueio> obter(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        return dados.values().stream()
            .filter(b -> b.getBloqueadorId().equals(bloqueadorId) && b.getBloqueadoId().equals(bloqueadoId))
            .findFirst();
    }

    @Override
    public boolean existeEntreAmbos(PerfilId a, PerfilId b) {
        return dados.values().stream()
            .filter(bl -> (bl.getBloqueadorId().equals(a) && bl.getBloqueadoId().equals(b))
                || (bl.getBloqueadorId().equals(b) && bl.getBloqueadoId().equals(a)))
            .findFirst().isPresent();
    }

    @Override
    public List<Bloqueio> listarEnvolvendo(PerfilId perfilId) {
        return dados.values().stream()
            .filter(b -> b.getBloqueadorId().equals(perfilId) || b.getBloqueadoId().equals(perfilId))
            .toList();
    }

    @Override
    public void limpar() {
        dados.clear();
    }
}

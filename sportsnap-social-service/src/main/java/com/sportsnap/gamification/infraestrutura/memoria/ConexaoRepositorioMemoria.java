package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.conexao.Conexao;
import com.sportsnap.gamification.dominio.conexao.ConexaoId;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ConexaoRepositorioMemoria implements ConexaoRepositorio {

    private final Map<ConexaoId, Conexao> dados = new ConcurrentHashMap<>();

    @Override
    public Conexao salvar(Conexao conexao) {
        dados.put(conexao.getId(), conexao);
        return conexao;
    }

    @Override
    public void remover(ConexaoId id) {
        dados.remove(id);
    }

    @Override
    public Optional<Conexao> obterPorPar(PerfilId seguidorId, PerfilId seguidoId) {
        return dados.values().stream()
            .filter(c -> c.getSeguidorId().equals(seguidorId) && c.getSeguidoId().equals(seguidoId))
            .findFirst();
    }

    @Override
    public List<Conexao> listarSeguidores(PerfilId seguidoId) {
        return dados.values().stream()
            .filter(c -> c.getSeguidoId().equals(seguidoId))
            .toList();
    }

    @Override
    public List<Conexao> listarSeguidos(PerfilId seguidorId) {
        return dados.values().stream()
            .filter(c -> c.getSeguidorId().equals(seguidorId))
            .toList();
    }

    @Override
    public void limpar() {
        dados.clear();
    }
}

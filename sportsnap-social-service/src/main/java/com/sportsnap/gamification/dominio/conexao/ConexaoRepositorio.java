package com.sportsnap.gamification.dominio.conexao;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface ConexaoRepositorio {

    Conexao salvar(Conexao conexao);

    void remover(ConexaoId id);

    Optional<Conexao> obterPorPar(PerfilId seguidorId, PerfilId seguidoId);

    List<Conexao> listarSeguidores(PerfilId seguidoId);

    List<Conexao> listarSeguidos(PerfilId seguidorId);

    void limpar();
}

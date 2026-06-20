package com.sportsnap.gamification.dominio.conexao;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface PedidoConexaoRepositorio {

    PedidoConexao salvar(PedidoConexao pedido);

    void remover(PedidoConexaoId id);

    Optional<PedidoConexao> obter(PedidoConexaoId id);

    Optional<PedidoConexao> obterPendentePorPar(PerfilId solicitanteId, PerfilId alvoId);

    List<PedidoConexao> listarPendentesPorAlvo(PerfilId alvoId);

    void removerPorPar(PerfilId solicitanteId, PerfilId alvoId);

    void limpar();
}

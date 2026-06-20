package com.sportsnap.gamification.dominio.conexao;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sportsnap.gamification.dominio.bloqueio.Bloqueio;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.perfil.Perfil;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;

public class ConexaoServico {

    private static final double W1_SEGUIDORES_COMUNS = 3.0;
    private static final double W2_MESMO_ESPORTE    = 2.0;
    private static final double W3_MESMA_LOCALIDADE = 1.0;

    private final PerfilRepositorio perfilRepositorio;
    private final ConexaoRepositorio conexaoRepositorio;
    private final PedidoConexaoRepositorio pedidoRepositorio;
    private final BloqueioRepositorio bloqueioRepositorio;
    private final EventoBarramento barramento;

    public ConexaoServico(PerfilRepositorio perfilRepositorio,
                          ConexaoRepositorio conexaoRepositorio,
                          PedidoConexaoRepositorio pedidoRepositorio,
                          BloqueioRepositorio bloqueioRepositorio,
                          EventoBarramento barramento) {
        notNull(perfilRepositorio,   "O repositorio de Perfil nao pode ser nulo");
        notNull(conexaoRepositorio,  "O repositorio de Conexao nao pode ser nulo");
        notNull(pedidoRepositorio,   "O repositorio de PedidoConexao nao pode ser nulo");
        notNull(bloqueioRepositorio, "O repositorio de Bloqueio nao pode ser nulo");
        notNull(barramento,          "O barramento de eventos nao pode ser nulo");
        this.perfilRepositorio   = perfilRepositorio;
        this.conexaoRepositorio  = conexaoRepositorio;
        this.pedidoRepositorio   = pedidoRepositorio;
        this.bloqueioRepositorio = bloqueioRepositorio;
        this.barramento          = barramento;
    }

    public void seguir(PerfilId seguidorId, PerfilId seguidoId) {
        notNull(seguidorId, "O seguidorId nao pode ser nulo");
        notNull(seguidoId,  "O seguidoId nao pode ser nulo");
        if (seguidorId.equals(seguidoId)) {
            throw new IllegalArgumentException("Um perfil nao pode seguir a si mesmo");
        }
        if (bloqueioRepositorio.existeEntreAmbos(seguidorId, seguidoId)) {
            throw new IllegalStateException("Operacao bloqueada entre esses perfis");
        }
        if (conexaoRepositorio.obterPorPar(seguidorId, seguidoId).isPresent()) {
            throw new IllegalStateException("Ja existe conexao entre esses perfis");
        }
        if (pedidoRepositorio.obterPendentePorPar(seguidorId, seguidoId).isPresent()) {
            throw new IllegalStateException("Ja existe pedido pendente para esse par");
        }
        var alvo = perfilRepositorio.obter(seguidoId)
            .orElseThrow(() -> new IllegalArgumentException("Perfil nao encontrado: " + seguidoId));

        if (alvo.isPublico()) {
            criarConexao(seguidorId, seguidoId);
        } else {
            pedidoRepositorio.salvar(new PedidoConexao(seguidorId, seguidoId));
        }
    }

    public Conexao aprovar(PedidoConexaoId pedidoId) {
        notNull(pedidoId, "O pedidoId nao pode ser nulo");
        var pedido = pedidoRepositorio.obter(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("PedidoConexao nao encontrado: " + pedidoId));
        pedido.aprovar();
        pedidoRepositorio.salvar(pedido);
        return criarConexao(pedido.getSolicitanteId(), pedido.getAlvoId());
    }

    public void recusar(PedidoConexaoId pedidoId) {
        notNull(pedidoId, "O pedidoId nao pode ser nulo");
        var pedido = pedidoRepositorio.obter(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("PedidoConexao nao encontrado: " + pedidoId));
        pedido.recusar();
        pedidoRepositorio.remover(pedidoId);
    }

    public void cancelar(PedidoConexaoId pedidoId, PerfilId canceladorId) {
        notNull(pedidoId,    "O pedidoId nao pode ser nulo");
        notNull(canceladorId, "O canceladorId nao pode ser nulo");
        var pedido = pedidoRepositorio.obter(pedidoId)
            .orElseThrow(() -> new IllegalArgumentException("PedidoConexao nao encontrado: " + pedidoId));
        if (!pedido.getSolicitanteId().equals(canceladorId)) {
            throw new IllegalStateException("Apenas o solicitante pode cancelar o pedido");
        }
        pedido.cancelar();
        pedidoRepositorio.remover(pedidoId);
        barramento.postar(new PedidoCanceladoEvento(pedidoId));
    }

    public void deixarDeSeguir(PerfilId seguidorId, PerfilId seguidoId) {
        notNull(seguidorId, "O seguidorId nao pode ser nulo");
        notNull(seguidoId,  "O seguidoId nao pode ser nulo");
        var conexao = conexaoRepositorio.obterPorPar(seguidorId, seguidoId)
            .orElseThrow(() -> new IllegalStateException("Nao existe conexao entre esses perfis"));
        conexaoRepositorio.remover(conexao.getId());
        atualizarContadores(seguidorId, seguidoId, -1);
    }

    public void bloquear(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        notNull(bloqueadorId, "O bloqueadorId nao pode ser nulo");
        notNull(bloqueadoId,  "O bloqueadoId nao pode ser nulo");
        if (bloqueadorId.equals(bloqueadoId)) {
            throw new IllegalArgumentException("Um perfil nao pode bloquear a si mesmo");
        }
        conexaoRepositorio.obterPorPar(bloqueadorId, bloqueadoId)
            .ifPresent(c -> { conexaoRepositorio.remover(c.getId()); atualizarContadores(bloqueadorId, bloqueadoId, -1); });
        conexaoRepositorio.obterPorPar(bloqueadoId, bloqueadorId)
            .ifPresent(c -> { conexaoRepositorio.remover(c.getId()); atualizarContadores(bloqueadoId, bloqueadorId, -1); });
        pedidoRepositorio.removerPorPar(bloqueadorId, bloqueadoId);
        pedidoRepositorio.removerPorPar(bloqueadoId, bloqueadorId);
        bloqueioRepositorio.salvar(new Bloqueio(bloqueadorId, bloqueadoId));
    }

    public void desbloquear(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        notNull(bloqueadorId, "O bloqueadorId nao pode ser nulo");
        notNull(bloqueadoId,  "O bloqueadoId nao pode ser nulo");
        bloqueioRepositorio.obter(bloqueadorId, bloqueadoId)
            .orElseThrow(() -> new IllegalStateException("Bloqueio nao encontrado"));
        bloqueioRepositorio.remover(bloqueadorId, bloqueadoId);
    }

    public List<SugestaoConexao> sugerirConexoes(PerfilId id, int n) {
        notNull(id, "O id do Perfil nao pode ser nulo");
        var meuPerfil = perfilRepositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Perfil nao encontrado: " + id));
        Set<PerfilId> meusSeguidos = conexaoRepositorio.listarSeguidos(id).stream()
            .map(Conexao::getSeguidoId).collect(Collectors.toSet());
        Set<PerfilId> bloqueados = bloqueioRepositorio.listarEnvolvendo(id).stream()
            .map(b -> b.getBloqueadorId().equals(id) ? b.getBloqueadoId() : b.getBloqueadorId())
            .collect(Collectors.toSet());

        return perfilRepositorio.listarTodos().stream()
            .filter(p -> !p.getId().equals(id))
            .filter(p -> !meusSeguidos.contains(p.getId()))
            .filter(p -> !bloqueados.contains(p.getId()))
            .map(candidato -> {
                long seguidoresEmComum = conexaoRepositorio.listarSeguidores(candidato.getId()).stream()
                    .map(Conexao::getSeguidorId)
                    .filter(meusSeguidos::contains)
                    .count();
                double mesmoEsporte    = Objects.equals(meuPerfil.getEsporte(),    candidato.getEsporte())    ? 1.0 : 0.0;
                double mesmaLocalidade = Objects.equals(meuPerfil.getLocalidade(), candidato.getLocalidade()) ? 1.0 : 0.0;
                double score = seguidoresEmComum * W1_SEGUIDORES_COMUNS
                             + mesmoEsporte      * W2_MESMO_ESPORTE
                             + mesmaLocalidade   * W3_MESMA_LOCALIDADE;
                return new SugestaoConexao(candidato, score);
            })
            .sorted(Comparator.comparingDouble(SugestaoConexao::getScore).reversed())
            .limit(n)
            .toList();
    }

    private Conexao criarConexao(PerfilId seguidorId, PerfilId seguidoId) {
        var conexao = conexaoRepositorio.salvar(new Conexao(seguidorId, seguidoId));
        atualizarContadores(seguidorId, seguidoId, +1);
        barramento.postar(new ConexaoCriadaEvento(conexao));
        return conexao;
    }

    private void atualizarContadores(PerfilId seguidorId, PerfilId seguidoId, int delta) {
        perfilRepositorio.obter(seguidorId).ifPresent(p -> {
            if (delta > 0) p.incrementarSeguindo(); else p.decrementarSeguindo();
            perfilRepositorio.salvar(p);
        });
        perfilRepositorio.obter(seguidoId).ifPresent(p -> {
            if (delta > 0) p.incrementarSeguidores(); else p.decrementarSeguidores();
            perfilRepositorio.salvar(p);
        });
    }

    // --- Eventos de domínio ---

    public static class ConexaoCriadaEvento {
        private final Conexao conexao;
        ConexaoCriadaEvento(Conexao conexao) { this.conexao = conexao; }
        public Conexao getConexao() { return conexao; }
    }

    public static class PedidoCanceladoEvento {
        private final PedidoConexaoId pedidoId;
        PedidoCanceladoEvento(PedidoConexaoId pedidoId) { this.pedidoId = pedidoId; }
        public PedidoConexaoId getPedidoId() { return pedidoId; }
    }
}

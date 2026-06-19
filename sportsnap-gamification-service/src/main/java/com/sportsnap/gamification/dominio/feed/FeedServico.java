package com.sportsnap.gamification.dominio.feed;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.conexao.Conexao;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class FeedServico {

    private static final int PAGINA_TAMANHO = 20;

    private final ItemFeedRepositorio itemFeedRepositorio;
    private final CurtidaRepositorio curtidaRepositorio;
    private final ConexaoRepositorio conexaoRepositorio;
    private final BloqueioRepositorio bloqueioRepositorio;
    private final EventoBarramento barramento;

    public FeedServico(ItemFeedRepositorio itemFeedRepositorio,
                       CurtidaRepositorio curtidaRepositorio,
                       ConexaoRepositorio conexaoRepositorio,
                       BloqueioRepositorio bloqueioRepositorio,
                       EventoBarramento barramento) {
        notNull(itemFeedRepositorio, "O repositorio de ItemFeed nao pode ser nulo");
        notNull(curtidaRepositorio,  "O repositorio de Curtida nao pode ser nulo");
        notNull(conexaoRepositorio,  "O repositorio de Conexao nao pode ser nulo");
        notNull(bloqueioRepositorio, "O repositorio de Bloqueio nao pode ser nulo");
        notNull(barramento,          "O barramento de eventos nao pode ser nulo");
        this.itemFeedRepositorio = itemFeedRepositorio;
        this.curtidaRepositorio  = curtidaRepositorio;
        this.conexaoRepositorio  = conexaoRepositorio;
        this.bloqueioRepositorio = bloqueioRepositorio;
        this.barramento          = barramento;
    }

    public ItemFeed publicarItem(PerfilId autorId, TipoItemFeed tipo, int referenciaId) {
        notNull(autorId, "O autorId nao pode ser nulo");
        notNull(tipo,    "O tipo nao pode ser nulo");
        return itemFeedRepositorio.salvar(new ItemFeed(autorId, tipo, referenciaId));
    }

    public List<ItemFeed> consultarFeed(PerfilId usuarioId, int pagina) {
        notNull(usuarioId, "O usuarioId nao pode ser nulo");
        List<PerfilId> seguidosIds = conexaoRepositorio.listarSeguidos(usuarioId).stream()
            .map(Conexao::getSeguidoId).toList();

        Set<PerfilId> bloqueadosIds = bloqueioRepositorio.listarEnvolvendo(usuarioId).stream()
            .map(b -> b.getBloqueadorId().equals(usuarioId) ? b.getBloqueadoId() : b.getBloqueadorId())
            .collect(Collectors.toSet());

        return itemFeedRepositorio.listarPorAutores(seguidosIds).stream()
            .filter(item -> !bloqueadosIds.contains(item.getAutorId()))
            .map(item -> {
                int curtidas = curtidaRepositorio.contarPorItem(item.getId());
                double grauConexao = calcularGrauConexao(usuarioId, item.getAutorId(), seguidosIds);
                double pontuacao = item.calcularPontuacao(curtidas, grauConexao);
                return new ItemFeedPontuado(item, pontuacao);
            })
            .sorted(Comparator.comparingDouble(ItemFeedPontuado::getPontuacao).reversed())
            .skip((long) pagina * PAGINA_TAMANHO)
            .limit(PAGINA_TAMANHO)
            .map(ItemFeedPontuado::getItem)
            .toList();
    }

    public Curtida curtir(PerfilId usuarioId, ItemFeedId itemId) {
        notNull(usuarioId, "O usuarioId nao pode ser nulo");
        notNull(itemId,    "O itemId nao pode ser nulo");
        var item = itemFeedRepositorio.obter(itemId)
            .orElseThrow(() -> new IllegalArgumentException("ItemFeed nao encontrado: " + itemId));
        var jaExiste = curtidaRepositorio.obterPorPar(usuarioId, itemId);
        if (jaExiste.isPresent()) {
            return jaExiste.get();
        }
        var curtida = curtidaRepositorio.salvar(new Curtida(usuarioId, itemId));
        barramento.postar(new ItemCurtidoEvento(curtida, item.getAutorId()));
        return curtida;
    }

    public void descurtir(PerfilId usuarioId, ItemFeedId itemId) {
        notNull(usuarioId, "O usuarioId nao pode ser nulo");
        notNull(itemId,    "O itemId nao pode ser nulo");
        curtidaRepositorio.obterPorPar(usuarioId, itemId)
            .ifPresent(c -> curtidaRepositorio.remover(c.getId()));
    }

    private double calcularGrauConexao(PerfilId usuarioId, PerfilId autorId,
                                        List<PerfilId> seguidosIds) {
        return seguidosIds.contains(autorId) ? 1.0 : 0.0;
    }

    // --- Eventos de domínio ---

    public static class ItemCurtidoEvento {
        private final Curtida curtida;
        private final PerfilId autorItemId;
        ItemCurtidoEvento(Curtida curtida, PerfilId autorItemId) {
            this.curtida     = curtida;
            this.autorItemId = autorItemId;
        }
        public Curtida getCurtida()     { return curtida; }
        public PerfilId getAutorItemId() { return autorItemId; }
    }

    private static class ItemFeedPontuado {
        private final ItemFeed item;
        private final double pontuacao;
        ItemFeedPontuado(ItemFeed item, double pontuacao) {
            this.item      = item;
            this.pontuacao = pontuacao;
        }
        ItemFeed getItem()        { return item; }
        double   getPontuacao()   { return pontuacao; }
    }
}

package com.sportsnap.gamification.dominio.ranking;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class RankingServico {

    private final CartaOficialRepositorio cartaRepositorio;

    public RankingServico(CartaOficialRepositorio cartaRepositorio) {
        notNull(cartaRepositorio, "O repositorio de CartaOficial nao pode ser nulo");
        this.cartaRepositorio = cartaRepositorio;
    }

    public double calcularOverall(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        var carta = cartaRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaId));
        return carta.calcularOverall();
    }

    public List<CartaOficial> consultarGlobal() {
        return cartaRepositorio.listarSincronizadasOrdenadasPorOverall();
    }

    public List<CartaOficial> consultarPorModalidade(String tipoEsporte) {
        notNull(tipoEsporte, "O tipo de esporte nao pode ser nulo");
        List<CartaOficial> sincronizadas = cartaRepositorio.listarSincronizadasOrdenadasPorOverall();
        return sincronizadas.stream()
            .filter(c -> !c.filtrarAtributosPorEsporte(tipoEsporte).isEmpty())
            .sorted(Comparator.comparingDouble((CartaOficial c) ->
                c.filtrarAtributosPorEsporte(tipoEsporte).stream()
                    .mapToDouble(a -> a.getValor() * a.getPeso()).sum()).reversed())
            .toList();
    }

    public Optional<Integer> consultarPosicao(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        List<CartaOficial> ranking = consultarGlobal();
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getAtletaId().equals(atletaId)) {
                return Optional.of(i + 1);
            }
        }
        return Optional.empty();
    }

    public List<CartaOficial> compararCartas(AtletaId atletaA, AtletaId atletaB) {
        notNull(atletaA, "O id do primeiro Atleta nao pode ser nulo");
        notNull(atletaB, "O id do segundo Atleta nao pode ser nulo");
        var a = cartaRepositorio.obterPorAtleta(atletaA)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaA));
        var b = cartaRepositorio.obterPorAtleta(atletaB)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaB));
        var resultado = new ArrayList<CartaOficial>();
        resultado.add(a);
        resultado.add(b);
        return resultado;
    }

    public RankingIterador criarIterador() {
        return new CartaRankingIterador(consultarGlobal());
    }

    public List<EntradaRanking> calcularRankingConcorrente(List<AtletaId> atletas, ExecutorService executor) {
        // ConcurrentHashMap é thread-safe — região crítica gerenciada internamente
        ConcurrentHashMap<AtletaId, Double> resultados = new ConcurrentHashMap<>();

        List<Future<?>> tarefas = new ArrayList<>();
        for (AtletaId atletaId : atletas) {
            tarefas.add(executor.submit(() -> {
                try {
                    resultados.put(atletaId, calcularOverall(atletaId));
                } catch (Exception e) {
                    resultados.put(atletaId, 0.0);
                }
            }));
        }

        for (Future<?> tarefa : tarefas) {
            try { tarefa.get(); } catch (Exception ignored) {}
        }

        return resultados.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .map(e -> new EntradaRanking(e.getKey().getId(), e.getValue()))
            .toList();
    }

    public record EntradaRanking(int atletaId, double overall) {}
}

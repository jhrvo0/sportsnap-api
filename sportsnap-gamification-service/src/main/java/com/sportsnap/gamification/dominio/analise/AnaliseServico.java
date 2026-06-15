package com.sportsnap.gamification.dominio.analise;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucao;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;

/**
 * Camada de inteligencia sobre a progressao (RN1 a RN13). Calcula percentis,
 * similaridade por distancia entre vetores, forca/fraqueza frente a media,
 * dados de radar normalizados e projecao de evolucao. Tudo recalculado sob
 * demanda, sem persistir estado derivado (RN12), e disponivel apenas para
 * cartas sincronizadas e ativas (RN1, RN13).
 */
public class AnaliseServico {

    private static final int BASE_MINIMA_PERCENTIL = 5;
    private static final int MIN_HISTORICO_PROJECAO = 3;

    private final CartaOficialRepositorio cartaRepositorio;
    private final RegistroEvolucaoRepositorio evolucaoRepositorio;
    private final MetricaSimilaridade metrica;

    public AnaliseServico(CartaOficialRepositorio cartaRepositorio,
                          RegistroEvolucaoRepositorio evolucaoRepositorio,
                          MetricaSimilaridade metrica) {
        notNull(cartaRepositorio, "O repositorio de CartaOficial nao pode ser nulo");
        notNull(evolucaoRepositorio, "O repositorio de RegistroEvolucao nao pode ser nulo");
        notNull(metrica, "A metrica de similaridade nao pode ser nula");
        this.cartaRepositorio = cartaRepositorio;
        this.evolucaoRepositorio = evolucaoRepositorio;
        this.metrica = metrica;
    }

    /** Percentil do atributo na base elegivel, entre 0 e 100 (RN2, RN3, RN4). */
    public PercentilAtributo percentil(AtletaId atletaId, String atributoNome) {
        notBlank(atributoNome, "O nome do atributo e obrigatorio");
        CartaOficial carta = obterCartaSincronizada(atletaId);
        double valor = valorDoAtributo(carta, atributoNome);

        List<Double> base = elegiveis().stream()
            .map(c -> valorDoAtributoOuNulo(c, atributoNome))
            .filter(v -> v != null)
            .toList();
        if (base.size() < BASE_MINIMA_PERCENTIL) {
            throw new IllegalStateException("RN3: amostra insuficiente para calcular percentil");
        }
        long abaixo = base.stream().filter(v -> v < valor).count();
        double percentil = (double) abaixo / base.size() * 100.0;
        return new PercentilAtributo(atributoNome, valor, percentil);
    }

    /** N atletas mais proximos pelo vetor de atributos da modalidade (RN5, RN6, RN7). */
    public List<RecomendacaoSimilaridade> similares(AtletaId atletaId, int n, String modalidade) {
        notBlank(modalidade, "A modalidade e obrigatoria");
        CartaOficial carta = obterCartaSincronizada(atletaId);
        List<String> dimensao = carta.filtrarAtributosPorEsporte(modalidade).stream()
            .map(AtributoEsportivo::getNome)
            .sorted()
            .toList();
        if (dimensao.isEmpty()) {
            return List.of();
        }
        double[] vetorAlvo = vetor(carta, dimensao);

        List<RecomendacaoSimilaridade> recomendacoes = new ArrayList<>();
        for (CartaOficial candidata : elegiveis()) {
            if (candidata.getAtletaId().equals(atletaId)) {
                continue; // RN6: nunca similar a si mesmo
            }
            double distancia = metrica.distancia(vetorAlvo, vetor(candidata, dimensao));
            recomendacoes.add(new RecomendacaoSimilaridade(candidata.getAtletaId(), distancia));
        }
        recomendacoes.sort((a, b) -> Double.compare(a.getDistancia(), b.getDistancia()));
        return recomendacoes.stream().limit(n).toList();
    }

    /** Classifica cada atributo da modalidade como forte, fraco ou neutro (RN8). */
    public List<ForcaFraqueza> forcaFraqueza(AtletaId atletaId, String modalidade) {
        notBlank(modalidade, "A modalidade e obrigatoria");
        CartaOficial carta = obterCartaSincronizada(atletaId);
        List<ForcaFraqueza> resultado = new ArrayList<>();
        for (AtributoEsportivo atributo : carta.filtrarAtributosPorEsporte(modalidade)) {
            List<Double> valores = elegiveis().stream()
                .map(c -> valorDoAtributoOuNulo(c, atributo.getNome()))
                .filter(v -> v != null)
                .toList();
            double media = media(valores);
            double desvio = desvioPadrao(valores, media);
            ClassificacaoForca classificacao;
            if (atributo.getValor() > media + desvio) {
                classificacao = ClassificacaoForca.FORTE;
            } else if (atributo.getValor() < media - desvio) {
                classificacao = ClassificacaoForca.FRACO;
            } else {
                classificacao = ClassificacaoForca.NEUTRO;
            }
            resultado.add(new ForcaFraqueza(atributo.getNome(), atributo.getValor(), media, classificacao));
        }
        return resultado;
    }

    /** Atributos do atleta normalizados para uma escala comum de 0 a 100 (RN9). */
    public DadosRadar radar(AtletaId atletaId) {
        CartaOficial carta = obterCartaSincronizada(atletaId);
        double maximo = carta.getAtributos().stream()
            .mapToDouble(AtributoEsportivo::getValor)
            .max()
            .orElse(0);
        Map<String, Double> normalizados = new LinkedHashMap<>();
        for (AtributoEsportivo atributo : carta.getAtributos()) {
            double valor = maximo > 0 ? atributo.getValor() / maximo * 100.0 : 0;
            normalizados.put(atributo.getNome(), valor);
        }
        return new DadosRadar(normalizados);
    }

    /** Projeta o Overall futuro a partir da tendencia do historico (RN10, RN11). */
    public Projecao projecao(AtletaId atletaId) {
        CartaOficial carta = obterCartaSincronizada(atletaId);
        List<RegistroEvolucao> historico = evolucaoRepositorio.listarPorAtleta(atletaId);
        if (historico.size() < MIN_HISTORICO_PROJECAO) {
            throw new IllegalStateException("RN10: historico insuficiente para projecao");
        }
        double tendencia = historico.stream().mapToDouble(RegistroEvolucao::getDelta).average().orElse(0);
        return new Projecao(carta.getOverall(), tendencia, carta.getOverall() + tendencia);
    }

    private List<CartaOficial> elegiveis() {
        return cartaRepositorio.listarTodas().stream()
            .filter(CartaOficial::isSincronizada)
            .filter(c -> !c.isArquivada())
            .toList();
    }

    private CartaOficial obterCartaSincronizada(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        CartaOficial carta = cartaRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("RN13: analise indisponivel, atleta sem carta"));
        if (!carta.isSincronizada() || carta.isArquivada()) {
            throw new IllegalStateException("RN13: analise indisponivel, carta nao sincronizada ou arquivada");
        }
        return carta;
    }

    private double[] vetor(CartaOficial carta, List<String> dimensao) {
        double[] v = new double[dimensao.size()];
        for (int i = 0; i < dimensao.size(); i++) {
            Double valor = valorDoAtributoOuNulo(carta, dimensao.get(i));
            v[i] = valor != null ? valor : 0;
        }
        return v;
    }

    private double valorDoAtributo(CartaOficial carta, String nome) {
        Double valor = valorDoAtributoOuNulo(carta, nome);
        if (valor == null) {
            throw new IllegalArgumentException("Atributo nao encontrado na carta: " + nome);
        }
        return valor;
    }

    private Double valorDoAtributoOuNulo(CartaOficial carta, String nome) {
        return carta.getAtributos().stream()
            .filter(a -> a.getNome().equalsIgnoreCase(nome))
            .map(AtributoEsportivo::getValor)
            .findFirst()
            .orElse(null);
    }

    private double media(List<Double> valores) {
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double desvioPadrao(List<Double> valores, double media) {
        if (valores.isEmpty()) {
            return 0;
        }
        double variancia = valores.stream()
            .mapToDouble(v -> (v - media) * (v - media))
            .average()
            .orElse(0);
        return Math.sqrt(variancia);
    }
}

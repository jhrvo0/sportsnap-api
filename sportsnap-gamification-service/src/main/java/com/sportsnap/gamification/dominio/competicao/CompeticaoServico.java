package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacaoRepositorio;

/**
 * Nucleo competitivo (RN27 a RN35, RN40 a RN44): elegibilidade, resolucao de
 * confrontos por Overall com desempate em camadas (RN30), variacao de PR estilo
 * Elo (RN31) com piso zero (RN32), classificacao (RN33), ligas (RN34),
 * matchmaking (RN41), decaimento por inatividade (RN35) e consultas de posicao.
 */
public class CompeticaoServico {

    public static final double PR_INICIAL = 1000.0;
    private static final double FAIXA_MATCHMAKING = 200.0;
    private static final long PERIODO_DECAIMENTO_DIAS = 7;
    private static final double TAXA_DECAIMENTO = 0.05;

    private final CartaOficialRepositorio cartaRepositorio;
    private final PontuacaoRankingRepositorio pontuacaoRepositorio;
    private final ConfrontoRepositorio confrontoRepositorio;
    private final TemporadaRepositorio temporadaRepositorio;
    private final RegistroSincronizacaoRepositorio sincronizacaoRepositorio;
    private final CalculoEloEstrategia elo;

    public CompeticaoServico(CartaOficialRepositorio cartaRepositorio,
                             PontuacaoRankingRepositorio pontuacaoRepositorio,
                             ConfrontoRepositorio confrontoRepositorio,
                             TemporadaRepositorio temporadaRepositorio,
                             RegistroSincronizacaoRepositorio sincronizacaoRepositorio,
                             CalculoEloEstrategia elo) {
        notNull(cartaRepositorio, "O repositorio de CartaOficial nao pode ser nulo");
        notNull(pontuacaoRepositorio, "O repositorio de PontuacaoRanking nao pode ser nulo");
        notNull(confrontoRepositorio, "O repositorio de Confronto nao pode ser nulo");
        notNull(temporadaRepositorio, "O repositorio de Temporada nao pode ser nulo");
        notNull(sincronizacaoRepositorio, "O repositorio de RegistroSincronizacao nao pode ser nulo");
        notNull(elo, "A estrategia de Elo nao pode ser nula");
        this.cartaRepositorio = cartaRepositorio;
        this.pontuacaoRepositorio = pontuacaoRepositorio;
        this.confrontoRepositorio = confrontoRepositorio;
        this.temporadaRepositorio = temporadaRepositorio;
        this.sincronizacaoRepositorio = sincronizacaoRepositorio;
        this.elo = elo;
    }

    /** Apenas cartas sincronizadas e nao arquivadas competem (RN27). */
    public boolean ehElegivel(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        return cartaRepositorio.obterPorAtleta(atletaId)
            .filter(CartaOficial::isSincronizada)
            .filter(c -> !c.isArquivada())
            .isPresent();
    }

    /** Torna o atleta elegivel com PR inicial padrao (RN27, RN28). */
    public PontuacaoRanking registrarElegivel(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        if (!ehElegivel(atletaId)) {
            throw new IllegalStateException("RN27: apenas cartas sincronizadas e ativas competem");
        }
        return pontuacaoRepositorio.obterPorAtleta(atletaId)
            .orElseGet(() -> pontuacaoRepositorio.salvar(new PontuacaoRanking(atletaId, PR_INICIAL)));
    }

    /** Resolve um confronto entre dois atletas distintos e elegiveis (RN29 a RN32, RN40). */
    public Confronto resolverConfronto(AtletaId atletaA, AtletaId atletaB, String modalidade,
                                       LocalDateTime quando) {
        notNull(atletaA, "O id do primeiro Atleta nao pode ser nulo");
        notNull(atletaB, "O id do segundo Atleta nao pode ser nulo");
        notBlank(modalidade, "A modalidade do confronto e obrigatoria");
        notNull(quando, "O instante do confronto nao pode ser nulo");

        if (atletaA.equals(atletaB)) {
            throw new IllegalArgumentException("RN29: confronto exige dois atletas distintos");
        }
        if (!ehElegivel(atletaA) || !ehElegivel(atletaB)) {
            throw new IllegalStateException("RN29: ambos os atletas devem ter carta sincronizada e ativa");
        }

        Temporada temporada = temporadaRepositorio.obterVigente(modalidade, quando)
            .orElseThrow(() -> new IllegalStateException(
                "RN40: confronto so conta para a temporada vigente da modalidade"));

        PontuacaoRanking prA = registrarElegivel(atletaA);
        PontuacaoRanking prB = registrarElegivel(atletaB);

        boolean aVence = atletaAVence(atletaA, atletaB);
        PontuacaoRanking vencedor = aVence ? prA : prB;
        PontuacaoRanking perdedor = aVence ? prB : prA;

        double variacao = elo.variacao(vencedor.getPr(), perdedor.getPr());
        vencedor.aplicarVariacao(variacao);
        perdedor.aplicarVariacao(-variacao);
        vencedor.marcarPartida(quando);
        perdedor.marcarPartida(quando);

        pontuacaoRepositorio.salvar(vencedor);
        pontuacaoRepositorio.salvar(perdedor);

        return confrontoRepositorio.salvar(new Confronto(
            vencedor.getAtletaId(), perdedor.getAtletaId(), variacao, quando, temporada.getId()));
    }

    /** Tabela de classificacao por PR decrescente, apenas dos elegiveis (RN33, RN43). */
    public List<PontuacaoRanking> classificacao() {
        return pontuacaoRepositorio.listarOrdenadasPorPr().stream()
            .filter(p -> ehElegivel(p.getAtletaId()))
            .toList();
    }

    /** Posicao e liga do atleta no ranking, ou vazio se nao classificado (RN42, RN43). */
    public Optional<PosicaoLiga> consultarPosicao(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        List<PontuacaoRanking> ranking = classificacao();
        for (int i = 0; i < ranking.size(); i++) {
            PontuacaoRanking p = ranking.get(i);
            if (p.getAtletaId().equals(atletaId)) {
                return Optional.of(new PosicaoLiga(atletaId, i + 1, p.getLiga(), p.getPr()));
            }
        }
        return Optional.empty();
    }

    /** Sugere oponentes com PR proxima, do mais proximo ao mais distante (RN41). */
    public List<PontuacaoRanking> sugerirOponentes(AtletaId atletaId, int limite) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        PontuacaoRanking alvo = pontuacaoRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("Atleta nao classificado: " + atletaId));
        double prAlvo = alvo.getPr();
        return classificacao().stream()
            .filter(p -> !p.getAtletaId().equals(atletaId))
            .filter(p -> Math.abs(p.getPr() - prAlvo) <= FAIXA_MATCHMAKING)
            .sorted(Comparator.comparingDouble(p -> Math.abs(p.getPr() - prAlvo)))
            .limit(limite)
            .toList();
    }

    /** Decai o PR dos atletas inativos alem do periodo definido (RN35). */
    public void aplicarDecaimentoInatividade(LocalDateTime referencia) {
        notNull(referencia, "O instante de referencia nao pode ser nulo");
        for (PontuacaoRanking p : pontuacaoRepositorio.listarOrdenadasPorPr()) {
            LocalDateTime ultima = p.getUltimaPartida();
            if (ultima != null && Duration.between(ultima, referencia).toDays() > PERIODO_DECAIMENTO_DIAS) {
                p.aplicarDecaimento(TAXA_DECAIMENTO);
                pontuacaoRepositorio.salvar(p);
            }
        }
    }

    /** Vencedor pelo maior Overall com desempate em camadas (RN30). */
    private boolean atletaAVence(AtletaId atletaA, AtletaId atletaB) {
        CartaOficial cartaA = obterCarta(atletaA);
        CartaOficial cartaB = obterCarta(atletaB);

        int porOverall = Double.compare(cartaA.getOverall(), cartaB.getOverall());
        if (porOverall != 0) {
            return porOverall > 0;
        }
        long sincA = sincronizacaoRepositorio.listarPorAtleta(atletaA).size();
        long sincB = sincronizacaoRepositorio.listarPorAtleta(atletaB).size();
        if (sincA != sincB) {
            return sincA > sincB;
        }
        int porData = compararDatas(cartaA.getUltimaSincronizacao(), cartaB.getUltimaSincronizacao());
        if (porData != 0) {
            return porData > 0;
        }
        return atletaA.getId() < atletaB.getId();
    }

    private int compararDatas(LocalDateTime a, LocalDateTime b) {
        LocalDateTime refA = a != null ? a : LocalDateTime.MIN;
        LocalDateTime refB = b != null ? b : LocalDateTime.MIN;
        return refA.compareTo(refB);
    }

    private CartaOficial obterCarta(AtletaId atletaId) {
        return cartaRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaId));
    }
}

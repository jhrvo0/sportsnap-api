package com.sportsnap.gamification.dominio.carta;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

public class CartaOficial {

    private final AtletaId atletaId;
    private final List<AtributoEsportivo> atributos;
    private double overall;
    private LocalDateTime ultimaSincronizacao;
    private TierCarta tier;
    private double saldoPontos;
    private boolean arquivada;

    public CartaOficial(AtletaId atletaId, List<AtributoEsportivo> atributos) {
        this(atletaId, atributos, 0, null, TierCarta.BRONZE, 0, false);
        this.overall = calcularOverall();
    }

    /** Reconstituicao simplificada (compatibilidade): tier derivado do Overall. */
    public CartaOficial(AtletaId atletaId, List<AtributoEsportivo> atributos,
                        double overall, LocalDateTime ultimaSincronizacao) {
        this(atletaId, atributos, overall, ultimaSincronizacao,
            TierCarta.paraOverall(overall), 0, false);
    }

    /** Reconstituicao completa a partir do repositorio. */
    public CartaOficial(AtletaId atletaId, List<AtributoEsportivo> atributos,
                        double overall, LocalDateTime ultimaSincronizacao,
                        TierCarta tier, double saldoPontos, boolean arquivada) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(atributos, "Os atributos nao podem ser nulos");
        notEmpty(atributos, "A CartaOficial deve ter pelo menos um atributo");
        notNull(tier, "O tier da carta nao pode ser nulo");
        isTrue(saldoPontos >= 0, "O saldo de pontos nao pode ser negativo");
        this.atletaId = atletaId;
        this.atributos = new ArrayList<>(atributos);
        this.overall = overall;
        this.ultimaSincronizacao = ultimaSincronizacao;
        this.tier = tier;
        this.saldoPontos = saldoPontos;
        this.arquivada = arquivada;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public Collection<AtributoEsportivo> getAtributos() {
        return new ArrayList<>(atributos);
    }

    public double getOverall() {
        return overall;
    }

    public LocalDateTime getUltimaSincronizacao() {
        return ultimaSincronizacao;
    }

    public boolean isSincronizada() {
        return ultimaSincronizacao != null;
    }

    public TierCarta getTier() {
        return tier;
    }

    public double getSaldoPontos() {
        return saldoPontos;
    }

    public boolean isArquivada() {
        return arquivada;
    }

    /**
     * Distribuicao uniforme de XP usada pela sincronizacao classica (H07). Mantida
     * para compatibilidade; o Reveal estrategico usa {@link #aplicarAlocacao}.
     */
    public void distribuirXp(double xpTotal) {
        garantirNaoArquivada();
        if (xpTotal <= 0) {
            throw new IllegalArgumentException("XP a distribuir deve ser positivo");
        }
        double xpPorAtributo = xpTotal / atributos.size();
        for (var atributo : atributos) {
            atributo.adicionarXp(xpPorAtributo);
        }
        this.overall = calcularOverall();
        this.ultimaSincronizacao = LocalDateTime.now();
    }

    /**
     * Aplica a alocacao estrategica do Reveal: eleva cada atributo pelo incremento
     * informado, recalcula o Overall (RN18) e promove o tier ao cruzar o limiar
     * (RN17). Respeita o teto do tier vigente (RN15) e o arquivamento (RN25). A
     * validacao de orcamento (RN14) e feita pelo MotorAlocacao antes da chamada.
     */
    public void aplicarAlocacao(Map<String, Integer> incrementos) {
        notNull(incrementos, "A alocacao nao pode ser nula");
        garantirNaoArquivada();
        int teto = tier.getTetoAtributo();
        for (var entrada : incrementos.entrySet()) {
            var atributo = atributoPorNome(entrada.getKey());
            int incremento = entrada.getValue();
            isTrue(incremento >= 0, "O incremento nao pode ser negativo");
            if (atributo.getValor() + incremento > teto) {
                throw new IllegalArgumentException(
                    "RN15: atributo '" + entrada.getKey() + "' ultrapassaria o teto do tier (" + teto + ")");
            }
            atributo.adicionarXp(incremento);
        }
        this.overall = calcularOverall();
        promoverSeElegivel();
        this.ultimaSincronizacao = LocalDateTime.now();
    }

    /** Pontos nao utilizados no Reveal viram saldo acumulavel (RN16). */
    public void adicionarSaldo(double pontos) {
        isTrue(pontos >= 0, "O saldo a adicionar nao pode ser negativo");
        this.saldoPontos += pontos;
    }

    /** Consome (zera) o saldo acumulado, devolvendo o valor para compor o orcamento. */
    public double consumirSaldo() {
        double total = saldoPontos;
        this.saldoPontos = 0;
        return total;
    }

    /** Arquiva a carta preservando o historico (RN26); sai do ranking (RN27). */
    public void arquivar() {
        this.arquivada = true;
    }

    public double calcularOverall() {
        double somaValoresPonderados = 0;
        double somaPesos = 0;
        for (var atributo : atributos) {
            somaValoresPonderados += atributo.getValor() * atributo.getPeso();
            somaPesos += atributo.getPeso();
        }
        return somaPesos > 0 ? somaValoresPonderados / somaPesos : 0;
    }

    public List<AtributoEsportivo> filtrarAtributosPorEsporte(String tipoEsporte) {
        notNull(tipoEsporte, "O tipo de esporte nao pode ser nulo");
        return atributos.stream()
            .filter(a -> a.getTipoEsporte().equalsIgnoreCase(tipoEsporte))
            .toList();
    }

    private void promoverSeElegivel() {
        TierCarta tierResultante = TierCarta.paraOverall(overall);
        if (tierResultante.ehSuperiorA(tier)) {
            this.tier = tierResultante;
        }
    }

    private void garantirNaoArquivada() {
        if (arquivada) {
            throw new IllegalStateException("RN25: carta arquivada nao aceita Reveal nem edicao");
        }
    }

    private AtributoEsportivo atributoPorNome(String nome) {
        notNull(nome, "O nome do atributo nao pode ser nulo");
        return atributos.stream()
            .filter(a -> a.getNome().equalsIgnoreCase(nome))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Atributo nao encontrado na carta: " + nome));
    }
}

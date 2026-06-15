package com.sportsnap.gamification.dominio.reveal;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Map;

import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;

/**
 * Servico de dominio que calcula o custo de uma alocacao de pontos sobre a carta
 * e valida as regras do Reveal: custo crescente por ponto (RN13), respeito ao
 * orcamento (RN14) e teto do tier atual (RN15). Nao altera estado.
 */
public class MotorAlocacao {

    private final CustoEvolucaoEstrategia custoEstrategia;

    public MotorAlocacao(CustoEvolucaoEstrategia custoEstrategia) {
        notNull(custoEstrategia, "A estrategia de custo nao pode ser nula");
        this.custoEstrategia = custoEstrategia;
    }

    /** Custo total (em pontos do orcamento) de aplicar os incrementos informados (RN13). */
    public int custoTotal(CartaOficial carta, Map<String, Integer> incrementos) {
        notNull(carta, "A carta nao pode ser nula");
        notNull(incrementos, "Os incrementos nao podem ser nulos");
        int total = 0;
        for (var entrada : incrementos.entrySet()) {
            var atributo = atributoOuFalha(carta, entrada.getKey());
            total += custoDeIncremento(atributo.getValor(), entrada.getValue());
        }
        return total;
    }

    /** Valida orcamento (RN14) e teto do tier atual (RN15) sem persistir. */
    public void validar(CartaOficial carta, Map<String, Integer> incrementos, Orcamento orcamento) {
        notNull(orcamento, "O orcamento nao pode ser nulo");
        int custo = custoTotal(carta, incrementos);
        if (custo > orcamento.getPontosDisponiveis()) {
            throw new IllegalArgumentException(
                "RN14: alocacao (" + custo + ") excede o orcamento disponivel ("
                    + orcamento.getPontosDisponiveis() + ")");
        }
        int teto = carta.getTier().getTetoAtributo();
        for (var entrada : incrementos.entrySet()) {
            var atributo = atributoOuFalha(carta, entrada.getKey());
            double valorFinal = atributo.getValor() + entrada.getValue();
            if (valorFinal > teto) {
                throw new IllegalArgumentException(
                    "RN15: atributo '" + entrada.getKey() + "' ultrapassaria o teto do tier ("
                        + teto + ")");
            }
        }
    }

    private int custoDeIncremento(double valorAtual, int incremento) {
        if (incremento < 0) {
            throw new IllegalArgumentException("O incremento nao pode ser negativo");
        }
        int custo = 0;
        for (int i = 0; i < incremento; i++) {
            custo += custoEstrategia.custoParaElevarUmPonto(valorAtual + i);
        }
        return custo;
    }

    private AtributoEsportivo atributoOuFalha(CartaOficial carta, String nome) {
        return carta.getAtributos().stream()
            .filter(a -> a.getNome().equalsIgnoreCase(nome))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Atributo nao encontrado na carta: " + nome));
    }
}

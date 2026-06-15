package com.sportsnap.gamification.dominio.reveal;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.carta.TierCarta;

/**
 * Objeto de valor que representa o orcamento de pontos liberado ao iniciar o
 * Reveal (RN12). E proporcional ao XP latente, modulado pelo tier da carta, e
 * acrescido do saldo nao utilizado em Reveals anteriores (RN16).
 */
public class Orcamento {

    private final int pontosDisponiveis;
    private final double xpLatente;
    private final TierCarta tier;

    private Orcamento(int pontosDisponiveis, double xpLatente, TierCarta tier) {
        this.pontosDisponiveis = pontosDisponiveis;
        this.xpLatente = xpLatente;
        this.tier = tier;
    }

    public static Orcamento calcular(double xpLatente, TierCarta tier, double saldoAcumulado) {
        notNull(tier, "O tier nao pode ser nulo");
        isTrue(xpLatente > 0, "O Reveal exige potencial latente positivo");
        isTrue(saldoAcumulado >= 0, "O saldo acumulado nao pode ser negativo");
        int pontos = (int) Math.floor(xpLatente * tier.getFatorOrcamento()) + (int) Math.floor(saldoAcumulado);
        return new Orcamento(pontos, xpLatente, tier);
    }

    public int getPontosDisponiveis() {
        return pontosDisponiveis;
    }

    public double getXpLatente() {
        return xpLatente;
    }

    public TierCarta getTier() {
        return tier;
    }
}

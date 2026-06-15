package com.sportsnap.gamification.dominio.carta;

/**
 * Tier (categoria) da Carta Oficial. Define o teto de atributo, o numero de
 * slots e o fator que modula o orcamento liberado no Reveal (RN07, RN12, RN15,
 * RN17). Os tiers sao ordenados do menor para o maior.
 */
public enum TierCarta {

    BRONZE(79, 3, 1.00, 0),
    PRATA(89, 4, 1.15, 80),
    OURO(99, 5, 1.30, 90),
    LENDARIA(99, 6, 1.50, 96);

    private final int tetoAtributo;
    private final int slots;
    private final double fatorOrcamento;
    private final double limiarPromocao;

    TierCarta(int tetoAtributo, int slots, double fatorOrcamento, double limiarPromocao) {
        this.tetoAtributo = tetoAtributo;
        this.slots = slots;
        this.fatorOrcamento = fatorOrcamento;
        this.limiarPromocao = limiarPromocao;
    }

    public int getTetoAtributo() {
        return tetoAtributo;
    }

    public int getSlots() {
        return slots;
    }

    public double getFatorOrcamento() {
        return fatorOrcamento;
    }

    public double getLimiarPromocao() {
        return limiarPromocao;
    }

    /** Tier correspondente a um Overall, cruzando os limiares de promocao (RN17). */
    public static TierCarta paraOverall(double overall) {
        TierCarta resultado = BRONZE;
        for (TierCarta tier : values()) {
            if (overall >= tier.limiarPromocao) {
                resultado = tier;
            }
        }
        return resultado;
    }

    public boolean ehSuperiorA(TierCarta outro) {
        return this.ordinal() > outro.ordinal();
    }
}

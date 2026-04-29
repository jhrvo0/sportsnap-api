package com.sportsnap.marketplace.dominio.dashboard;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.licenca.Dinheiro;

public class ResumoFotografo {

    private final FotografoId fotografoId;
    private final int totalLotes;
    private final int totalFotos;
    private final int totalVendas;
    private final Dinheiro receitaBruta;
    private final Dinheiro saldoDisponivel;

    public ResumoFotografo(FotografoId fotografoId, int totalLotes, int totalFotos,
                            int totalVendas, Dinheiro receitaBruta, Dinheiro saldoDisponivel) {
        notNull(fotografoId, "O id do Fotografo nao pode ser nulo");
        notNull(receitaBruta, "A receita bruta nao pode ser nula");
        notNull(saldoDisponivel, "O saldo disponivel nao pode ser nulo");
        this.fotografoId = fotografoId;
        this.totalLotes = totalLotes;
        this.totalFotos = totalFotos;
        this.totalVendas = totalVendas;
        this.receitaBruta = receitaBruta;
        this.saldoDisponivel = saldoDisponivel;
    }

    public FotografoId getFotografoId() {
        return fotografoId;
    }

    public int getTotalLotes() {
        return totalLotes;
    }

    public int getTotalFotos() {
        return totalFotos;
    }

    public int getTotalVendas() {
        return totalVendas;
    }

    public Dinheiro getReceitaBruta() {
        return receitaBruta;
    }

    public Dinheiro getSaldoDisponivel() {
        return saldoDisponivel;
    }
}

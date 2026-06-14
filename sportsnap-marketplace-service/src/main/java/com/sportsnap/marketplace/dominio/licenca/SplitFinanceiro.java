package com.sportsnap.marketplace.dominio.licenca;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

public class SplitFinanceiro {

    private final LicencaId licencaId;
    private final Dinheiro valorFotografo;
    private final Dinheiro taxaPlataforma;
    private final LocalDateTime processadoEm;

    public SplitFinanceiro(LicencaId licencaId, Dinheiro valorFotografo, Dinheiro taxaPlataforma) {
        notNull(licencaId, "O id da Licenca nao pode ser nulo");
        notNull(valorFotografo, "O valor do fotografo nao pode ser nulo");
        notNull(taxaPlataforma, "A taxa da plataforma nao pode ser nula");
        this.licencaId = licencaId;
        this.valorFotografo = valorFotografo;
        this.taxaPlataforma = taxaPlataforma;
        this.processadoEm = LocalDateTime.now();
    }

    public LicencaId getLicencaId() {
        return licencaId;
    }

    public Dinheiro getValorFotografo() {
        return valorFotografo;
    }

    public Dinheiro getTaxaPlataforma() {
        return taxaPlataforma;
    }

    public LocalDateTime getProcessadoEm() {
        return processadoEm;
    }
}

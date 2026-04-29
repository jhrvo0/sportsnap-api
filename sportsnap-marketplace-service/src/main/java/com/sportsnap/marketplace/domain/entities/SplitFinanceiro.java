package com.sportsnap.marketplace.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SplitFinanceiro {

    private Long id;
    private BigDecimal valorFotografo;
    private BigDecimal taxaPlataforma;
    private LocalDateTime processadoEm;
    private LicencaDeImagem licencaDeImagem;

    public SplitFinanceiro() {}

    public SplitFinanceiro(BigDecimal valorFotografo, BigDecimal taxaPlataforma, LicencaDeImagem licencaDeImagem) {
        this.valorFotografo = valorFotografo;
        this.taxaPlataforma = taxaPlataforma;
        this.licencaDeImagem = licencaDeImagem;
        this.processadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getValorFotografo() { return valorFotografo; }
    public void setValorFotografo(BigDecimal valorFotografo) { this.valorFotografo = valorFotografo; }

    public BigDecimal getTaxaPlataforma() { return taxaPlataforma; }
    public void setTaxaPlataforma(BigDecimal taxaPlataforma) { this.taxaPlataforma = taxaPlataforma; }

    public LocalDateTime getProcessadoEm() { return processadoEm; }
    public void setProcessadoEm(LocalDateTime processadoEm) { this.processadoEm = processadoEm; }

    public LicencaDeImagem getLicencaDeImagem() { return licencaDeImagem; }
    public void setLicencaDeImagem(LicencaDeImagem licencaDeImagem) { this.licencaDeImagem = licencaDeImagem; }
}

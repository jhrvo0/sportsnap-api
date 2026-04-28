package com.sportsnap.marketplace.domain.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "splits_financeiros")
public class SplitFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFotografo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaPlataforma;

    @Column(nullable = false)
    private LocalDateTime processadoEm;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licenca_id", nullable = false, unique = true)
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

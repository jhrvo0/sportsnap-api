package com.sportsnap.marketplace.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LicencaDeImagem {

    private Long id;
    private Long atletaId;
    private BigDecimal preco;
    private LocalDateTime adquiridaEm;
    private Foto foto;
    private SplitFinanceiro splitFinanceiro;

    public LicencaDeImagem() {}

    public LicencaDeImagem(Long atletaId, BigDecimal preco, Foto foto) {
        this.atletaId = atletaId;
        this.preco = preco;
        this.foto = foto;
        this.adquiridaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAtletaId() { return atletaId; }
    public void setAtletaId(Long atletaId) { this.atletaId = atletaId; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public LocalDateTime getAdquiridaEm() { return adquiridaEm; }
    public void setAdquiridaEm(LocalDateTime adquiridaEm) { this.adquiridaEm = adquiridaEm; }

    public Foto getFoto() { return foto; }
    public void setFoto(Foto foto) { this.foto = foto; }

    public SplitFinanceiro getSplitFinanceiro() { return splitFinanceiro; }
    public void setSplitFinanceiro(SplitFinanceiro splitFinanceiro) { this.splitFinanceiro = splitFinanceiro; }
}

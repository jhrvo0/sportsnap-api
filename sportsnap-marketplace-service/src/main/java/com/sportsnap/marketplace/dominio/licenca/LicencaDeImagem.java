package com.sportsnap.marketplace.dominio.licenca;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;

public class LicencaDeImagem {

    private final LicencaId id;
    private final AtletaId atletaId;
    private final FotoId fotoId;
    private final Dinheiro preco;
    private final LocalDateTime adquiridaEm;
    private boolean cancelada;

    public LicencaDeImagem(AtletaId atletaId, FotoId fotoId, Dinheiro preco) {
        id = null;
        this.atletaId = validarAtleta(atletaId);
        this.fotoId = validarFoto(fotoId);
        this.preco = validarPreco(preco);
        this.adquiridaEm = LocalDateTime.now();
        this.cancelada = false;
    }

    public LicencaDeImagem(LicencaId id, AtletaId atletaId, FotoId fotoId, Dinheiro preco,
                            LocalDateTime adquiridaEm, boolean cancelada) {
        notNull(id, "O id da Licenca nao pode ser nulo");
        this.id = id;
        this.atletaId = validarAtleta(atletaId);
        this.fotoId = validarFoto(fotoId);
        this.preco = validarPreco(preco);
        notNull(adquiridaEm, "A data de aquisicao nao pode ser nula");
        this.adquiridaEm = adquiridaEm;
        this.cancelada = cancelada;
    }

    private AtletaId validarAtleta(AtletaId id) {
        notNull(id, "A Licenca precisa de um Atleta valido");
        return id;
    }

    private FotoId validarFoto(FotoId id) {
        notNull(id, "A Licenca precisa de uma Foto valida");
        return id;
    }

    private Dinheiro validarPreco(Dinheiro preco) {
        notNull(preco, "O preco da Licenca nao pode ser nulo");
        return preco;
    }

    public LicencaId getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public FotoId getFotoId() {
        return fotoId;
    }

    public Dinheiro getPreco() {
        return preco;
    }

    public LocalDateTime getAdquiridaEm() {
        return adquiridaEm;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void cancelar(LocalDateTime agora) {
        notNull(agora, "O instante de cancelamento nao pode ser nulo");
        if (cancelada) {
            throw new IllegalStateException("A Licenca ja esta cancelada");
        }
        if (agora.isAfter(adquiridaEm.plusDays(7))) {
            throw new IllegalStateException("Janela de cancelamento de 7 dias expirada");
        }
        this.cancelada = true;
    }
}

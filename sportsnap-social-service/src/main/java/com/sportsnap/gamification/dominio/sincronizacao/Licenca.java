package com.sportsnap.gamification.dominio.sincronizacao;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Licenca {

    private final AtletaId atletaId;
    private final LocalDateTime adquiridaEm;

    public Licenca(AtletaId atletaId, LocalDateTime adquiridaEm) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(adquiridaEm, "A data de aquisicao nao pode ser nula");
        this.atletaId = atletaId;
        this.adquiridaEm = adquiridaEm;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public LocalDateTime getAdquiridaEm() {
        return adquiridaEm;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Licenca outra
            && atletaId.equals(outra.atletaId)
            && adquiridaEm.equals(outra.adquiridaEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(atletaId, adquiridaEm);
    }
}

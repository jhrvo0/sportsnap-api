package com.sportsnap.session.dominio.sessao;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.session.dominio.spot.SpotId;

public class Sessao {

    private final SessaoId id;
    private final SpotId spotId;
    private Periodo periodo;
    private String descricao;
    private boolean cancelada;

    public Sessao(SpotId spotId, Periodo periodo, String descricao) {
        id = null;
        this.spotId = validarSpot(spotId);
        setPeriodo(periodo);
        setDescricao(descricao);
        this.cancelada = false;
    }

    public Sessao(SessaoId id, SpotId spotId, Periodo periodo, String descricao, boolean cancelada) {
        notNull(id, "O id da Sessao nao pode ser nulo");
        this.id = id;
        this.spotId = validarSpot(spotId);
        setPeriodo(periodo);
        setDescricao(descricao);
        this.cancelada = cancelada;
    }

    private SpotId validarSpot(SpotId spotId) {
        notNull(spotId, "A Sessao precisa de um Spot valido");
        return spotId;
    }

    public SessaoId getId() {
        return id;
    }

    public SpotId getSpotId() {
        return spotId;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        notNull(periodo, "O periodo da Sessao nao pode ser nulo");
        this.periodo = periodo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        notBlank(descricao, "A descricao da Sessao nao pode estar em branco");
        this.descricao = descricao;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public boolean estaAtiva(LocalDateTime agora) {
        notNull(agora, "O instante nao pode ser nulo");
        if (cancelada) {
            return false;
        }
        return periodo.contem(agora);
    }

    public void cancelar(LocalDateTime agora) {
        notNull(agora, "O instante nao pode ser nulo");
        if (cancelada) {
            throw new IllegalStateException("A Sessao ja esta cancelada");
        }
        if (periodo.jaIniciou(agora)) {
            throw new IllegalStateException("A Sessao nao pode ser cancelada apos iniciar");
        }
        this.cancelada = true;
    }
}

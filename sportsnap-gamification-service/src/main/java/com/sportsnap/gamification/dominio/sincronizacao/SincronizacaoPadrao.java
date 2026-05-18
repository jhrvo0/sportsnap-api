package com.sportsnap.gamification.dominio.sincronizacao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;

public class SincronizacaoPadrao extends TemplateSincronizacao {

    private final CartaOficialRepositorio cartaRepositorio;
    private final StatusPotencialRepositorio statusRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final EventoBarramento barramento;

    public SincronizacaoPadrao(CartaOficialRepositorio cartaRepositorio,
                                StatusPotencialRepositorio statusRepositorio,
                                LicencaRepositorio licencaRepositorio,
                                EventoBarramento barramento) {
        notNull(cartaRepositorio);
        notNull(statusRepositorio);
        notNull(licencaRepositorio);
        notNull(barramento);
        this.cartaRepositorio = cartaRepositorio;
        this.statusRepositorio = statusRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.barramento = barramento;
    }

    @Override
    protected void validarElegibilidade(AtletaId atletaId) {
        var status = statusRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("StatusPotencial nao encontrado: " + atletaId));
        if (status.getXpAcumulado() <= 0) {
            throw new IllegalStateException("Atleta nao possui XP acumulado para sincronizar");
        }
        var carta = cartaRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaId));
        var ultimaSync = carta.getUltimaSincronizacao();
        var referencia = ultimaSync != null ? ultimaSync : LocalDateTime.MIN;
        if (!licencaRepositorio.existeLicencaPosterior(atletaId, referencia)) {
            throw new IllegalStateException("RN01: sincronizacao requer licenca posterior a ultima Reveal");
        }
    }

    @Override
    protected double obterXpAcumulado(AtletaId atletaId) {
        StatusPotencial status = statusRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("StatusPotencial nao encontrado: " + atletaId));
        double xp = status.zerar();
        statusRepositorio.salvar(status);
        return xp;
    }

    @Override
    protected CartaOficial transferirXpParaCarta(AtletaId atletaId, double xp) {
        CartaOficial carta = cartaRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaId));
        carta.distribuirXp(xp);
        return cartaRepositorio.salvar(carta);
    }

    @Override
    protected void notificar(CartaOficial carta, double xp) {
        barramento.postar(new SincronizacaoServico.CartaSincronizadaEvento(carta, xp));
    }
}

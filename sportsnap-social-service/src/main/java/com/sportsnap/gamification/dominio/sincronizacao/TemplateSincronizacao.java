package com.sportsnap.gamification.dominio.sincronizacao;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.CartaOficial;

public abstract class TemplateSincronizacao {

    public final CartaOficial executar(AtletaId atletaId) {
        validarElegibilidade(atletaId);
        double xp = obterXpAcumulado(atletaId);
        CartaOficial carta = transferirXpParaCarta(atletaId, xp);
        notificar(carta, xp);
        return carta;
    }

    protected abstract void validarElegibilidade(AtletaId atletaId);

    protected abstract double obterXpAcumulado(AtletaId atletaId);

    protected abstract CartaOficial transferirXpParaCarta(AtletaId atletaId, double xp);

    protected abstract void notificar(CartaOficial carta, double xp);
}

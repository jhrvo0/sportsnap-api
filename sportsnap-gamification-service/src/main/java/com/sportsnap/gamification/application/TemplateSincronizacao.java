package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.entities.StatusPotencial;

/**
 * Pattern: Template Method
 *
 * Define o esqueleto do algoritmo de sincronizacao da carta do atleta.
 * Subclasses podem sobrescrever passos especificos sem alterar a estrutura geral.
 *
 * O metodo sincronizar() define a sequencia fixa:
 * 1. validar → 2. transferirXp → 3. recalcularOverall → 4. finalizarSincronizacao
 */
public abstract class TemplateSincronizacao {

    // Template Method: define a sequencia fixa do algoritmo
    public final void sincronizar(Atleta atleta, CartaOficial carta, StatusPotencial status) {
        validar(atleta, status);
        transferirXp(carta, status);
        recalcularOverall(carta);
        finalizarSincronizacao(carta, status);
    }

    protected abstract void validar(Atleta atleta, StatusPotencial status);

    protected abstract void transferirXp(CartaOficial carta, StatusPotencial status);

    protected abstract void recalcularOverall(CartaOficial carta);

    protected abstract void finalizarSincronizacao(CartaOficial carta, StatusPotencial status);
}

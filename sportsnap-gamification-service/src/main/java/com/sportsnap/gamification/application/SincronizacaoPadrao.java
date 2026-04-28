package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.entities.StatusPotencial;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// Pattern: Template Method — Implementacao padrao do processo de sincronizacao
@Component
public class SincronizacaoPadrao extends TemplateSincronizacao {

    @Override
    protected void validar(Atleta atleta, StatusPotencial status) {
        if (status.getXpAcumulado() <= 0) {
            throw new IllegalStateException("Atleta nao possui XP acumulado para sincronizar");
        }
    }

    @Override
    protected void transferirXp(CartaOficial carta, StatusPotencial status) {
        List<AtributoEsportivo> atributos = carta.getAtributos();
        if (!atributos.isEmpty()) {
            double xpPorAtributo = status.getXpAcumulado() / atributos.size();
            for (AtributoEsportivo atributo : atributos) {
                atributo.setValor(atributo.getValor() + xpPorAtributo);
            }
        }
    }

    @Override
    protected void recalcularOverall(CartaOficial carta) {
        List<AtributoEsportivo> atributos = carta.getAtributos();
        double somaValoresPonderados = 0;
        double somaPesos = 0;
        for (AtributoEsportivo atributo : atributos) {
            somaValoresPonderados += atributo.getValor() * atributo.getPeso();
            somaPesos += atributo.getPeso();
        }
        carta.setOverall(somaPesos > 0 ? somaValoresPonderados / somaPesos : 0);
    }

    @Override
    protected void finalizarSincronizacao(CartaOficial carta, StatusPotencial status) {
        carta.setUltimaSincronizacao(LocalDateTime.now());
        status.setXpAcumulado(0.0);
    }
}

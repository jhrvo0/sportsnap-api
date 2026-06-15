package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sportsnap.gamification.dominio.atleta.Atleta;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.atleta.Email;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.TierCarta;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;

@Component
class JpaMapeador {

    Atleta paraDominio(AtletaJpa jpa) {
        return new Atleta(new AtletaId(jpa.id), jpa.nome, new Email(jpa.email));
    }

    AtletaJpa paraJpa(Atleta dominio) {
        var jpa = new AtletaJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.nome = dominio.getNome();
        jpa.email = dominio.getEmail().getEndereco();
        return jpa;
    }

    CartaOficial paraDominio(CartaOficialJpa jpa) {
        var atletaId = new AtletaId(jpa.atletaId);
        List<AtributoEsportivo> atributos = jpa.atributos.stream()
            .map(a -> new AtributoEsportivo(a.nome, a.valor, a.peso, a.tipoEsporte))
            .toList();
        var tier = jpa.tier != null ? TierCarta.valueOf(jpa.tier) : TierCarta.paraOverall(jpa.overall);
        return new CartaOficial(atletaId, atributos, jpa.overall, jpa.ultimaSincronizacao,
            tier, jpa.saldoPontos, jpa.arquivada);
    }

    CartaOficialJpa paraJpa(CartaOficial dominio) {
        var jpa = new CartaOficialJpa();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.overall = dominio.getOverall();
        jpa.ultimaSincronizacao = dominio.getUltimaSincronizacao();
        jpa.tier = dominio.getTier().name();
        jpa.saldoPontos = dominio.getSaldoPontos();
        jpa.arquivada = dominio.isArquivada();
        jpa.atributos = dominio.getAtributos().stream()
            .map(a -> {
                var aJpa = new AtributoEsportivoJpa();
                aJpa.nome = a.getNome();
                aJpa.valor = a.getValor();
                aJpa.peso = a.getPeso();
                aJpa.tipoEsporte = a.getTipoEsporte();
                return aJpa;
            })
            .collect(Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    StatusPotencial paraDominio(StatusPotencialJpa jpa) {
        return new StatusPotencial(new AtletaId(jpa.atletaId),
            jpa.xpAcumulado, jpa.streakConsistencia, jpa.ultimaAtividade);
    }

    StatusPotencialJpa paraJpa(StatusPotencial dominio) {
        var jpa = new StatusPotencialJpa();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.xpAcumulado = dominio.getXpAcumulado();
        jpa.streakConsistencia = dominio.getStreakConsistencia();
        jpa.ultimaAtividade = dominio.getUltimaAtividade();
        return jpa;
    }

    Licenca paraDominio(LicencaJpa jpa) {
        return new Licenca(new AtletaId(jpa.atletaId), jpa.adquiridaEm);
    }

    LicencaJpa paraJpa(Licenca dominio) {
        var jpa = new LicencaJpa();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.adquiridaEm = dominio.getAdquiridaEm();
        return jpa;
    }
}

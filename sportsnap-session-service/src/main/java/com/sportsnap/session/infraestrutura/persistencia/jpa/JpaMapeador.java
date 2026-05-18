package com.sportsnap.session.infraestrutura.persistencia.jpa;

import org.springframework.stereotype.Component;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.atividade.Intensidade;
import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotId;

@Component
class JpaMapeador {

    Spot paraDominio(SpotJpa jpa) {
        return new Spot(new SpotId(jpa.id), jpa.nome, new Coordenada(jpa.latitude, jpa.longitude), jpa.descricao);
    }

    SpotJpa paraJpa(Spot dominio) {
        var jpa = new SpotJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.nome = dominio.getNome();
        jpa.latitude = dominio.getCoordenada().getLatitude();
        jpa.longitude = dominio.getCoordenada().getLongitude();
        jpa.descricao = dominio.getDescricao();
        return jpa;
    }

    Sessao paraDominio(SessaoJpa jpa) {
        var spotId = new SpotId(jpa.spotId);
        var periodo = new Periodo(jpa.periodoInicio, jpa.periodoFim);
        return new Sessao(new SessaoId(jpa.id), spotId, periodo, jpa.descricao, jpa.cancelada);
    }

    SessaoJpa paraJpa(Sessao dominio) {
        var jpa = new SessaoJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.spotId = dominio.getSpotId().getId();
        jpa.periodoInicio = dominio.getPeriodo().getInicio();
        jpa.periodoFim = dominio.getPeriodo().getFim();
        jpa.descricao = dominio.getDescricao();
        jpa.cancelada = dominio.isCancelada();
        return jpa;
    }

    CheckIn paraDominio(CheckInJpa jpa) {
        var atletaId = new AtletaId(jpa.atletaId);
        var sessaoId = new SessaoId(jpa.sessaoId);
        var coordenada = new Coordenada(jpa.latitude, jpa.longitude);
        return new CheckIn(new CheckInId(jpa.id), atletaId, sessaoId,
            jpa.horario, coordenada, jpa.cancelado, jpa.atividadeRegistrada);
    }

    CheckInJpa paraJpa(CheckIn dominio) {
        var jpa = new CheckInJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.sessaoId = dominio.getSessaoId().getId();
        jpa.horario = dominio.getHorario();
        jpa.latitude = dominio.getCoordenada().getLatitude();
        jpa.longitude = dominio.getCoordenada().getLongitude();
        jpa.cancelado = dominio.isCancelado();
        jpa.atividadeRegistrada = dominio.temAtividadeRegistrada();
        return jpa;
    }

    RegistroAtividade paraDominio(RegistroAtividadeJpa jpa) {
        var checkInId = new CheckInId(jpa.checkInId);
        var intensidade = Intensidade.valueOf(jpa.intensidade);
        return new RegistroAtividade(new RegistroAtividadeId(jpa.id), checkInId,
            jpa.distancia, jpa.duracaoSegundos, intensidade, jpa.xpCalculado);
    }

    RegistroAtividadeJpa paraJpa(RegistroAtividade dominio) {
        var jpa = new RegistroAtividadeJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.checkInId = dominio.getCheckInId().getId();
        jpa.distancia = dominio.getDistancia();
        jpa.duracaoSegundos = dominio.getDuracaoSegundos();
        jpa.intensidade = dominio.getIntensidade().name();
        jpa.xpCalculado = dominio.getXpCalculado();
        return jpa;
    }
}

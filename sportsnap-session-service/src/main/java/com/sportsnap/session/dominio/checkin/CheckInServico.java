package com.sportsnap.session.dominio.checkin;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn.CheckInRealizadoEvento;
import com.sportsnap.session.dominio.evento.EventoBarramento;
import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.spot.Coordenada;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckInServico {

    private final CheckInRepositorio repositorio;
    private final SessaoRepositorio sessaoRepositorio;
    private final EventoBarramento barramento;

    public CheckInServico(CheckInRepositorio repositorio,
                           SessaoRepositorio sessaoRepositorio,
                           EventoBarramento barramento) {
        notNull(repositorio, "O repositorio de CheckIn nao pode ser nulo");
        notNull(sessaoRepositorio, "O repositorio de Sessao nao pode ser nulo");
        notNull(barramento, "O barramento de eventos nao pode ser nulo");
        this.repositorio = repositorio;
        this.sessaoRepositorio = sessaoRepositorio;
        this.barramento = barramento;
    }

    public CheckIn realizar(AtletaId atletaId, SessaoId sessaoId, Coordenada coordenada) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(sessaoId, "O id da Sessao nao pode ser nulo");
        notNull(coordenada, "A coordenada nao pode ser nula");

        Sessao sessao = sessaoRepositorio.obter(sessaoId)
            .orElseThrow(() -> new IllegalArgumentException("Sessao nao encontrada: " + sessaoId));

        LocalDateTime agora = LocalDateTime.now();
        if (!sessao.estaAtiva(agora)) {
            throw new IllegalStateException("Sessao encerrada");
        }

        var duplicado = repositorio.obterPorAtletaESessao(atletaId, sessaoId);
        if (duplicado.isPresent() && !duplicado.get().isCancelado()) {
            throw new IllegalStateException("Atleta ja possui CheckIn ativo nesta Sessao");
        }

        var checkIn = new CheckIn(atletaId, sessaoId, agora, coordenada);
        var salvo = repositorio.salvar(checkIn);
        barramento.postar(new CheckInRealizadoEvento(salvo));
        return salvo;
    }

    public CheckIn obter(CheckInId id) {
        notNull(id, "O id do CheckIn nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("CheckIn nao encontrado: " + id));
    }

    public List<CheckIn> listarPorAtleta(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        return repositorio.listarPorAtleta(atletaId);
    }

    public List<CheckIn> listarPorSessao(SessaoId sessaoId) {
        notNull(sessaoId, "O id da Sessao nao pode ser nulo");
        return repositorio.listarPorSessao(sessaoId);
    }

    public void cancelar(CheckInId id) {
        var checkIn = obter(id);
        var evento = checkIn.cancelar();
        repositorio.salvar(checkIn);
        barramento.postar(evento);
    }

    public List<AtletaId> listarAtletasComCheckIn(SessaoId sessaoId) {
        notNull(sessaoId, "O id da Sessao nao pode ser nulo");

        var sessao = sessaoRepositorio.obter(sessaoId)
            .orElseThrow(() -> new IllegalArgumentException("Sessao nao encontrada: " + sessaoId));

        var checkIns = repositorio.listarPorSessao(sessaoId);
        Set<AtletaId> unicos = new HashSet<>();
        for (CheckIn checkIn : checkIns) {
            if (checkIn.isCancelado()) {
                continue;
            }
            if (sessao.getPeriodo().contem(checkIn.getHorario())) {
                unicos.add(checkIn.getAtletaId());
            }
        }
        return new ArrayList<>(unicos);
    }
}

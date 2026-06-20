package com.sportsnap.session.dominio.sessao;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.dominio.spot.SpotRepositorio;

import java.time.LocalDateTime;
import java.util.List;

public class SessaoServico {

    private final SessaoRepositorio repositorio;
    private final SpotRepositorio spotRepositorio;

    public SessaoServico(SessaoRepositorio repositorio, SpotRepositorio spotRepositorio) {
        notNull(repositorio, "O repositorio de Sessao nao pode ser nulo");
        notNull(spotRepositorio, "O repositorio de Spot nao pode ser nulo");
        this.repositorio = repositorio;
        this.spotRepositorio = spotRepositorio;
    }

    public Sessao cadastrar(SpotId spotId, Periodo periodo, String descricao) {
        spotRepositorio.obter(spotId)
            .orElseThrow(() -> new IllegalArgumentException("Spot nao encontrado para cadastro de Sessao: " + spotId));
        validarInicioFuturo(periodo);
        var sessao = new Sessao(spotId, periodo, descricao);
        return repositorio.salvar(sessao);
    }

    public Sessao obter(SessaoId id) {
        notNull(id, "O id da Sessao nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Sessao nao encontrada: " + id));
    }

    public List<Sessao> listarAtivas() {
        return repositorio.listarAtivas(LocalDateTime.now());
    }

    public List<Sessao> listarPorSpot(SpotId spotId) {
        notNull(spotId, "O id do Spot nao pode ser nulo");
        return repositorio.listarPorSpot(spotId);
    }

    public List<Sessao> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        notNull(inicio, "O inicio do periodo nao pode ser nulo");
        notNull(fim, "O fim do periodo nao pode ser nulo");
        return repositorio.listarPorPeriodo(inicio, fim);
    }

    public void cancelar(SessaoId id) {
        var sessao = obter(id);
        sessao.cancelar(LocalDateTime.now());
        repositorio.salvar(sessao);
    }

    public Sessao atualizar(SessaoId id, SpotId spotId, Periodo periodo, String descricao) {
        notNull(id, "O id da Sessao nao pode ser nulo");
        var existente = obter(id);
        var agora = LocalDateTime.now();
        if (existente.isCancelada()) {
            throw new IllegalStateException("Sessao cancelada nao pode ser editada");
        }
        if (existente.getPeriodo().terminou(agora)) {
            throw new IllegalStateException("Sessao encerrada nao pode ser editada");
        }
        if (existente.getPeriodo().jaIniciou(agora)
            && !existente.getPeriodo().getInicio().equals(periodo.getInicio())) {
            throw new IllegalStateException("Sessao em andamento nao pode alterar inicio");
        }
        if (!existente.getPeriodo().jaIniciou(agora)) {
            validarInicioFuturo(periodo);
        }
        var atualizado = new Sessao(existente.getId(), spotId, periodo, descricao, existente.isCancelada());
        return repositorio.salvar(atualizado);
    }

    public void remover(SessaoId id) {
        var sessao = obter(id);
        if (!sessao.getPeriodo().terminou(LocalDateTime.now())) {
            throw new IllegalStateException("Somente Sessao encerrada pode ser removida");
        }
        repositorio.remover(id);
    }

    private void validarInicioFuturo(Periodo periodo) {
        notNull(periodo, "O periodo da Sessao nao pode ser nulo");
        if (periodo.getInicio().isBefore(LocalDateTime.now().minusSeconds(5))) {
            throw new IllegalArgumentException("O inicio da Sessao nao pode ficar no passado");
        }
    }
}

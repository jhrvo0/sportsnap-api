package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.isTrue;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.checkin.CheckInRepositorio;
import com.sportsnap.session.dominio.evento.EventoBarramento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AtividadeServico {

    private final RegistroAtividadeRepositorio repositorio;
    private final CheckInRepositorio checkInRepositorio;
    private final EventoBarramento barramento;

    public AtividadeServico(RegistroAtividadeRepositorio repositorio,
                             CheckInRepositorio checkInRepositorio,
                             EventoBarramento barramento) {
        notNull(repositorio, "O repositorio de RegistroAtividade nao pode ser nulo");
        notNull(checkInRepositorio, "O repositorio de CheckIn nao pode ser nulo");
        notNull(barramento, "O barramento de eventos nao pode ser nulo");
        this.repositorio = repositorio;
        this.checkInRepositorio = checkInRepositorio;
        this.barramento = barramento;
    }

    public RegistroAtividade registrar(CheckInId checkInId, double distancia, long duracaoSegundos,
                                        Intensidade intensidade) {
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");

        CheckIn checkIn = checkInRepositorio.obter(checkInId)
            .orElseThrow(() -> new IllegalArgumentException("CheckIn nao encontrado: " + checkInId));

        return registrarComCheckIn(
            checkIn.getAtletaId(),
            checkInId,
            "CORRIDA",
            checkIn.getHorario(),
            distancia,
            duracaoSegundos,
            intensidade,
            distancia * intensidade.getMultiplicador(),
            null,
            null,
            "CHECKIN",
            null
        );
    }

    public RegistroAtividade registrarComCheckIn(AtletaId atletaId, CheckInId checkInId, String esporte,
                                                 LocalDateTime data, double distancia, long duracaoSegundos,
                                                 Intensidade intensidade, double xpCalculado,
                                                 Integer esforcoPercebido, String observacoes,
                                                 String origemRegistro, String metricasJson) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        notBlank(esporte, "O esporte nao pode estar em branco");
        notNull(data, "A data nao pode ser nula");
        isTrue(distancia >= 0, "A distancia nao pode ser negativa");
        isTrue(duracaoSegundos > 0, "A duracao deve ser positiva");

        CheckIn checkIn = checkInRepositorio.obter(checkInId)
            .orElseThrow(() -> new IllegalArgumentException("CheckIn nao encontrado: " + checkInId));

        if (checkIn.isCancelado()) {
            throw new IllegalStateException("Nao e possivel registrar atividade em CheckIn cancelado");
        }

        var registro = new RegistroAtividade(
            null, // id
            atletaId,
            checkInId,
            esporte.toUpperCase().trim(),
            data,
            distancia,
            duracaoSegundos,
            intensidade,
            xpCalculado,
            esforcoPercebido,
            observacoes,
            origemRegistro,
            metricasJson,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        var salvo = repositorio.salvar(registro);

        checkIn.marcarAtividadeRegistrada();
        checkInRepositorio.salvar(checkIn);

        barramento.postar(new AtividadeRegistradaEvento(salvo, atletaId));
        return salvo;
    }



    public RegistroAtividade atualizar(RegistroAtividadeId id, String esporte, LocalDateTime data,
                                       double distancia, long duracaoSegundos, Integer esforcoPercebido,
                                       String observacoes) {
        return atualizar(id, esporte, data, distancia, duracaoSegundos, null, 0.0, esforcoPercebido, observacoes, null);
    }

    public RegistroAtividade atualizar(RegistroAtividadeId id, String esporte, LocalDateTime data,
                                       double distancia, long duracaoSegundos, Intensidade intensidade,
                                       double xpCalculado, Integer esforcoPercebido, String observacoes,
                                       String metricasJson) {
        notNull(id, "O id do RegistroAtividade nao pode ser nulo");
        var existente = repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("RegistroAtividade nao encontrado: " + id));

        notBlank(esporte, "O esporte nao pode estar em branco");
        notNull(data, "A data nao pode ser nula");
        isTrue(distancia >= 0, "A distancia nao pode ser negativa");
        isTrue(duracaoSegundos > 0, "A duracao deve ser positiva");
        if (esforcoPercebido != null) {
            isTrue(esforcoPercebido >= 1 && esforcoPercebido <= 10, "O esforco percebido deve ser de 1 a 10");
        }

        var atualizado = new RegistroAtividade(
            existente.getId(),
            existente.getAtletaId(),
            existente.getCheckInId(),
            esporte.toUpperCase().trim(),
            data,
            distancia,
            duracaoSegundos,
            intensidade,
            xpCalculado,
            esforcoPercebido,
            observacoes,
            existente.getOrigemRegistro(),
            metricasJson,
            existente.getCriadoEm(),
            LocalDateTime.now()
        );

        return repositorio.salvar(atualizado);
    }

    public void remover(RegistroAtividadeId id) {
        notNull(id, "O id do RegistroAtividade nao pode ser nulo");
        repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("RegistroAtividade nao encontrado: " + id));
        repositorio.remover(id);
    }

    public Optional<RegistroAtividade> obter(RegistroAtividadeId id) {
        notNull(id, "O id do RegistroAtividade nao pode ser nulo");
        return repositorio.obter(id);
    }

    public List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId) {
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        return repositorio.listarPorCheckIn(checkInId);
    }

    public List<RegistroAtividade> listarPorAtleta(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        return repositorio.buscarPorAtleta(atletaId);
    }

    public List<RegistroAtividade> listarPorAtletaEEsporte(AtletaId atletaId, String esporte) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notBlank(esporte, "O esporte nao pode estar em branco");
        return repositorio.buscarPorAtletaEEsporte(atletaId, esporte);
    }

    public List<RegistroAtividade> listarPorAtletaEsporteEPeriodo(AtletaId atletaId, String esporte, LocalDateTime inicio, LocalDateTime fim) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notBlank(esporte, "O esporte nao pode estar em branco");
        notNull(inicio, "A data de inicio nao pode ser nula");
        notNull(fim, "A data de fim nao pode ser nula");
        return repositorio.buscarPorAtletaEsporteEPeriodo(atletaId, esporte, inicio, fim);
    }

    public double calcularXpTotalDoCheckIn(CheckInId checkInId) {
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        return repositorio.listarPorCheckIn(checkInId).stream()
            .mapToDouble(RegistroAtividade::getXpCalculado)
            .sum();
    }

    public static class AtividadeRegistradaEvento {
        private final RegistroAtividade registro;
        private final AtletaId atletaId;

        AtividadeRegistradaEvento(RegistroAtividade registro, AtletaId atletaId) {
            this.registro = registro;
            this.atletaId = atletaId;
        }

        public RegistroAtividade getRegistro() {
            return registro;
        }

        public AtletaId getAtletaId() {
            return atletaId;
        }
    }
}

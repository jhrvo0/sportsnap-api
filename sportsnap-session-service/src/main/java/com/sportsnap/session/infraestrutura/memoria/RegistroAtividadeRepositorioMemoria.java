package com.sportsnap.session.infraestrutura.memoria;

import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeId;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeRepositorio;
import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckInId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RegistroAtividadeRepositorioMemoria implements RegistroAtividadeRepositorio {

    private final Map<Integer, RegistroAtividade> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public RegistroAtividade salvar(RegistroAtividade registro) {
        RegistroAtividadeId id = registro.getId();
        if (id == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new RegistroAtividade(new RegistroAtividadeId(novoId),
                                              registro.getAtletaId(),
                                              registro.getCheckInId(),
                                              registro.getEsporte(),
                                              registro.getData(),
                                              registro.getDistancia(),
                                              registro.getDuracaoSegundos(),
                                              registro.getIntensidade(),
                                              registro.getXpCalculado(),
                                              registro.getEsforcoPercebido(),
                                              registro.getObservacoes(),
                                              registro.getOrigemRegistro(),
                                              registro.getMetricas(),
                                              registro.getCriadoEm(),
                                              registro.getAtualizadoEm());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(id.getId(), registro);
        return registro;
    }

    @Override
    public Optional<RegistroAtividade> obter(RegistroAtividadeId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId) {
        return armazem.values().stream()
            .filter(r -> r.getCheckInId() != null && r.getCheckInId().equals(checkInId))
            .collect(Collectors.toList());
    }

    @Override
    public List<RegistroAtividade> buscarPorAtleta(AtletaId atletaId) {
        return armazem.values().stream()
            .filter(r -> r.getAtletaId() != null && r.getAtletaId().equals(atletaId))
            .collect(Collectors.toList());
    }

    @Override
    public List<RegistroAtividade> buscarPorAtletaEEsporte(AtletaId atletaId, String esporte) {
        return armazem.values().stream()
            .filter(r -> r.getAtletaId() != null && r.getAtletaId().equals(atletaId) &&
                         r.getEsporte().equalsIgnoreCase(esporte))
            .collect(Collectors.toList());
    }

    @Override
    public List<RegistroAtividade> buscarPorAtletaEsporteEPeriodo(AtletaId atletaId, String esporte, LocalDateTime inicio, LocalDateTime fim) {
        return armazem.values().stream()
            .filter(r -> r.getAtletaId() != null && r.getAtletaId().equals(atletaId) &&
                         r.getEsporte().equalsIgnoreCase(esporte) &&
                         r.getData() != null && !r.getData().isBefore(inicio) && !r.getData().isAfter(fim))
            .collect(Collectors.toList());
    }

    @Override
    public void remover(RegistroAtividadeId id) {
        if (id != null) {
            armazem.remove(id.getId());
        }
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

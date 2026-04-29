package com.sportsnap.session.infraestrutura.memoria;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.checkin.CheckInRepositorio;
import com.sportsnap.session.dominio.sessao.SessaoId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class CheckInRepositorioMemoria implements CheckInRepositorio {

    private final Map<Integer, CheckIn> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public CheckIn salvar(CheckIn checkIn) {
        CheckInId id = checkIn.getId();
        if (id == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new CheckIn(new CheckInId(novoId),
                                    checkIn.getAtletaId(),
                                    checkIn.getSessaoId(),
                                    checkIn.getHorario(),
                                    checkIn.getCoordenada(),
                                    checkIn.isCancelado(),
                                    checkIn.temAtividadeRegistrada());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(id.getId(), checkIn);
        return checkIn;
    }

    @Override
    public Optional<CheckIn> obter(CheckInId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<CheckIn> listarPorAtleta(AtletaId atletaId) {
        return armazem.values().stream()
            .filter(c -> c.getAtletaId().equals(atletaId))
            .collect(Collectors.toList());
    }

    @Override
    public List<CheckIn> listarPorSessao(SessaoId sessaoId) {
        return armazem.values().stream()
            .filter(c -> c.getSessaoId().equals(sessaoId))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<CheckIn> obterPorAtletaESessao(AtletaId atletaId, SessaoId sessaoId) {
        return armazem.values().stream()
            .filter(c -> c.getAtletaId().equals(atletaId) && c.getSessaoId().equals(sessaoId))
            .findFirst();
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

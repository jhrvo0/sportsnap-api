package com.sportsnap.session.infraestrutura.memoria;

import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.spot.SpotId;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class SessaoRepositorioMemoria implements SessaoRepositorio {

    private final Map<Integer, Sessao> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public Sessao salvar(Sessao sessao) {
        SessaoId id = sessao.getId();
        if (id == null) {
            int novoId = sequencia.getAndIncrement();
            var nova = new Sessao(new SessaoId(novoId), sessao.getSpotId(), sessao.getPeriodo(),
                                   sessao.getDescricao(), sessao.isCancelada());
            armazem.put(novoId, nova);
            return nova;
        }
        armazem.put(id.getId(), sessao);
        return sessao;
    }

    @Override
    public Optional<Sessao> obter(SessaoId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<Sessao> listarPorSpot(SpotId spotId) {
        return armazem.values().stream()
            .filter(s -> s.getSpotId().equals(spotId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Sessao> listarAtivas(LocalDateTime agora) {
        return armazem.values().stream()
            .filter(s -> s.estaAtiva(agora))
            .collect(Collectors.toList());
    }

    @Override
    public List<Sessao> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return armazem.values().stream()
            .filter(s -> !s.getPeriodo().getInicio().isAfter(fim)
                      && !s.getPeriodo().getFim().isBefore(inicio))
            .collect(Collectors.toList());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

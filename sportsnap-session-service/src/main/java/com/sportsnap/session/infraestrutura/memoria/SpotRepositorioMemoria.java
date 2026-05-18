package com.sportsnap.session.infraestrutura.memoria;

import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.dominio.spot.SpotRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// @Repository (desativado: usando JPA)
public class SpotRepositorioMemoria implements SpotRepositorio {

    private final Map<Integer, Spot> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public Spot salvar(Spot spot) {
        SpotId id = spot.getId();
        if (id == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new Spot(new SpotId(novoId), spot.getNome(),
                                spot.getCoordenada(), spot.getDescricao());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(id.getId(), spot);
        return spot;
    }

    @Override
    public Optional<Spot> obter(SpotId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<Spot> listarTodos() {
        return new ArrayList<>(armazem.values());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

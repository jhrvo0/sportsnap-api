package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.atleta.Atleta;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class AtletaRepositorioMemoria implements AtletaRepositorio {

    private final Map<Integer, Atleta> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public Atleta salvar(Atleta atleta) {
        if (atleta.getId() == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new Atleta(new AtletaId(novoId), atleta.getNome(), atleta.getEmail());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(atleta.getId().getId(), atleta);
        return atleta;
    }

    @Override
    public Optional<Atleta> obter(AtletaId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<Atleta> listarTodos() {
        return new ArrayList<>(armazem.values());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

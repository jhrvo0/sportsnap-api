package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// @Repository (desativado: usando JPA)
public class FotografoRepositorioMemoria implements FotografoRepositorio {

    private final Map<Integer, Fotografo> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public Fotografo salvar(Fotografo fotografo) {
        if (fotografo.getId() == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new Fotografo(new FotografoId(novoId), fotografo.getNome(), fotografo.getEmail());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(fotografo.getId().getId(), fotografo);
        return fotografo;
    }

    @Override
    public Optional<Fotografo> obter(FotografoId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<Fotografo> listarTodos() {
        return new ArrayList<>(armazem.values());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

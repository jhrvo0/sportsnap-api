package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.sugestao.FavoritoRepositorio;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// @Repository (desativado: usando JPA)
public class FavoritoRepositorioMemoria implements FavoritoRepositorio {

    private final Map<Integer, Set<Integer>> armazem = new ConcurrentHashMap<>();

    @Override
    public void adicionar(AtletaId atletaId, FotoId fotoId) {
        armazem.computeIfAbsent(atletaId.getId(), k -> ConcurrentHashMap.newKeySet())
            .add(fotoId.getId());
    }

    @Override
    public void remover(AtletaId atletaId, FotoId fotoId) {
        var set = armazem.get(atletaId.getId());
        if (set != null) {
            set.remove(fotoId.getId());
        }
    }

    @Override
    public List<FotoId> listarPorAtleta(AtletaId atletaId) {
        var set = armazem.getOrDefault(atletaId.getId(), new HashSet<>());
        return set.stream().map(FotoId::new).collect(Collectors.toList());
    }

    @Override
    public void limpar() {
        armazem.clear();
    }
}

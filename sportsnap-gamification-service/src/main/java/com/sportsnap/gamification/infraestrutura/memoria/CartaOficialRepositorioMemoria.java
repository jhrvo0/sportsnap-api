package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// @Repository (desativado: usando JPA)
public class CartaOficialRepositorioMemoria implements CartaOficialRepositorio {

    private final Map<Integer, CartaOficial> porAtleta = new ConcurrentHashMap<>();

    @Override
    public CartaOficial salvar(CartaOficial carta) {
        porAtleta.put(carta.getAtletaId().getId(), carta);
        return carta;
    }

    @Override
    public Optional<CartaOficial> obterPorAtleta(AtletaId atletaId) {
        if (atletaId == null) return Optional.empty();
        return Optional.ofNullable(porAtleta.get(atletaId.getId()));
    }

    @Override
    public List<CartaOficial> listarTodas() {
        return porAtleta.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<CartaOficial> listarSincronizadasOrdenadasPorOverall() {
        return porAtleta.values().stream()
            .filter(CartaOficial::isSincronizada)
            .sorted(Comparator.comparingDouble(CartaOficial::getOverall).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public void limpar() {
        porAtleta.clear();
    }
}

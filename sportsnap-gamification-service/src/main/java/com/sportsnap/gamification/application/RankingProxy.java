package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.infrastructure.persistence.JpaCartaOficialRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pattern: Proxy
 *
 * Proxy com cache para consultas ao Ranking.
 * Evita consultas repetidas ao banco de dados quando o ranking
 * nao mudou. O cache expira apos 30 segundos.
 *
 * O Proxy intercepta a chamada real ao repositorio e retorna
 * dados do cache quando disponivel, reduzindo carga no banco.
 */
@Component
public class RankingProxy {

    private static final long CACHE_TTL_MS = 30_000; // 30 segundos

    private final JpaCartaOficialRepository cartaOficialRepository;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public RankingProxy(JpaCartaOficialRepository cartaOficialRepository) {
        this.cartaOficialRepository = cartaOficialRepository;
    }

    public List<CartaOficial> getRanking() {
        String chave = "ranking_geral";
        CacheEntry entry = cache.get(chave);

        if (entry != null && !entry.expirou()) {
            // Cache hit — retorna sem consultar o banco
            return entry.dados;
        }

        // Cache miss — consulta o banco e armazena
        List<CartaOficial> ranking = cartaOficialRepository.findAllByOrderByOverallDesc();
        cache.put(chave, new CacheEntry(ranking));
        return ranking;
    }

    public void invalidarCache() {
        cache.clear();
    }

    private static class CacheEntry {
        final List<CartaOficial> dados;
        final long criadoEm;

        CacheEntry(List<CartaOficial> dados) {
            this.dados = dados;
            this.criadoEm = System.currentTimeMillis();
        }

        boolean expirou() {
            return System.currentTimeMillis() - criadoEm > CACHE_TTL_MS;
        }
    }
}

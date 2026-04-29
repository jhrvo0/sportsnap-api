package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class LoteRepositorioMemoria implements LoteRepositorio {

    private final Map<Integer, Lote> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public Lote salvar(Lote lote) {
        if (lote.getId() == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new Lote(new LoteId(novoId), lote.getFotografoId(), lote.getSessaoId(),
                                 lote.getSpotId(), lote.getDescricao(), lote.getCriadoEm(),
                                 lote.isArquivado());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(lote.getId().getId(), lote);
        return lote;
    }

    @Override
    public Optional<Lote> obter(LoteId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<Lote> listarPorFotografo(FotografoId fotografoId) {
        return armazem.values().stream()
            .filter(l -> l.getFotografoId().equals(fotografoId))
            .collect(Collectors.toList());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

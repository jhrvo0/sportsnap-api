package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class FotoRepositorioMemoria implements FotoRepositorio {

    private final Map<Integer, Foto> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public Foto salvar(Foto foto) {
        if (foto.getId() == null) {
            int novoId = sequencia.getAndIncrement();
            var nova = new Foto(new FotoId(novoId), foto.getLoteId(), foto.getUrlPreview(),
                                 foto.getUrlOriginal(), foto.getExif(),
                                 foto.isLicenciada(), foto.isRemovida());
            armazem.put(novoId, nova);
            return nova;
        }
        armazem.put(foto.getId().getId(), foto);
        return foto;
    }

    @Override
    public Optional<Foto> obter(FotoId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<Foto> listarPorLote(LoteId loteId) {
        return armazem.values().stream()
            .filter(f -> f.getLoteId().equals(loteId) && !f.isRemovida())
            .collect(Collectors.toList());
    }

    @Override
    public List<Foto> listarTodas() {
        return new ArrayList<>(armazem.values());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

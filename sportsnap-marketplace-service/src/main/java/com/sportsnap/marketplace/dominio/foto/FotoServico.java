package com.sportsnap.marketplace.dominio.foto;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FotoServico {

    private final FotoRepositorio repositorio;
    private final LoteRepositorio loteRepositorio;

    public FotoServico(FotoRepositorio repositorio, LoteRepositorio loteRepositorio) {
        notNull(repositorio, "O repositorio de Foto nao pode ser nulo");
        notNull(loteRepositorio, "O repositorio de Lote nao pode ser nulo");
        this.repositorio = repositorio;
        this.loteRepositorio = loteRepositorio;
    }

    public List<Foto> uploadEmLote(LoteId loteId, List<String> caminhosFotos) {
        notNull(loteId, "O id do Lote nao pode ser nulo");
        notNull(caminhosFotos, "A lista de caminhos nao pode ser nula");
        notEmpty(caminhosFotos, "A lista de caminhos nao pode estar vazia");

        Lote lote = loteRepositorio.obter(loteId)
            .orElseThrow(() -> new IllegalArgumentException("Lote nao encontrado: " + loteId));

        if (lote.isArquivado()) {
            throw new IllegalStateException("Lote arquivado nao aceita novas fotos");
        }

        var fotosSalvas = new ArrayList<Foto>();
        for (String caminho : caminhosFotos) {
            String nome = caminho.substring(caminho.lastIndexOf('/') + 1);
            var exif = extrairExif(caminho);
            var foto = new Foto(loteId, "preview_" + nome, caminho, exif);
            fotosSalvas.add(repositorio.salvar(foto));
        }
        return fotosSalvas;
    }

    public Foto obter(FotoId id) {
        notNull(id, "O id da Foto nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada: " + id));
    }

    public List<Foto> listarPorLote(LoteId loteId) {
        notNull(loteId, "O id do Lote nao pode ser nulo");
        return repositorio.listarPorLote(loteId);
    }

    public void remover(FotoId id) {
        var foto = obter(id);
        foto.remover();
        repositorio.salvar(foto);
    }

    private MetadadosExif extrairExif(String caminho) {
        return new MetadadosExif(LocalDateTime.now(),
            "EXIF{camera=Canon EOS R5, iso=400, aperture=f/2.8, gps=-23.5505,-46.6333, file=" + caminho + "}");
    }
}

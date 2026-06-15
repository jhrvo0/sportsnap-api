package com.sportsnap.marketplace.dominio.lote;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;

import java.util.List;

public class LoteServico {

    private final LoteRepositorio repositorio;
    private final FotografoRepositorio fotografoRepositorio;
    private final FotoRepositorio fotoRepositorio;

    public LoteServico(LoteRepositorio repositorio, FotografoRepositorio fotografoRepositorio, FotoRepositorio fotoRepositorio) {
        notNull(repositorio, "O repositorio de Lote nao pode ser nulo");
        notNull(fotografoRepositorio, "O repositorio de Fotografo nao pode ser nulo");
        notNull(fotoRepositorio, "O repositorio de Foto nao pode ser nulo");
        this.repositorio = repositorio;
        this.fotografoRepositorio = fotografoRepositorio;
        this.fotoRepositorio = fotoRepositorio;
    }

    public Lote cadastrar(FotografoId fotografoId, SessaoId sessaoId, SpotId spotId, String descricao) {
        fotografoRepositorio.obter(fotografoId)
            .orElseThrow(() -> new IllegalArgumentException("Fotografo nao encontrado para cadastro de Lote: " + fotografoId));
        var lote = new Lote(fotografoId, sessaoId, spotId, descricao);
        return repositorio.salvar(lote);
    }

    public Lote obter(LoteId id) {
        notNull(id, "O id do Lote nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Lote nao encontrado: " + id));
    }

    public List<Lote> listarPorFotografo(FotografoId fotografoId) {
        notNull(fotografoId, "O id do Fotografo nao pode ser nulo");
        return repositorio.listarPorFotografo(fotografoId);
    }

    public void editarDescricao(LoteId id, String novaDescricao) {
        var lote = obter(id);
        lote.setDescricao(novaDescricao);
        repositorio.salvar(lote);
    }

    public void arquivar(LoteId id) {
        var lote = obter(id);
        lote.arquivar();
        repositorio.salvar(lote);
    }

    public void desarquivar(LoteId id) {
        var lote = obter(id);
        lote.desarquivar();
        repositorio.salvar(lote);
    }

    public void excluir(LoteId id) {
        obter(id);
        // Remove todas as fotos do lote antes de excluir o lote
        fotoRepositorio.listarPorLote(id).forEach(foto -> {
            foto.remover();
            fotoRepositorio.salvar(foto);
        });
        repositorio.remover(id);
    }
}

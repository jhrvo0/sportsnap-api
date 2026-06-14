package com.sportsnap.marketplace.dominio.lote;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.marketplace.dominio.fotografo.FotografoId;

public class Lote {

    private final LoteId id;
    private final FotografoId fotografoId;
    private final SessaoId sessaoId;
    private final SpotId spotId;
    private final LocalDateTime criadoEm;
    private String descricao;
    private boolean arquivado;

    public Lote(FotografoId fotografoId, SessaoId sessaoId, SpotId spotId, String descricao) {
        id = null;
        this.fotografoId = validarFotografo(fotografoId);
        this.sessaoId = validarSessao(sessaoId);
        this.spotId = validarSpot(spotId);
        setDescricao(descricao);
        this.criadoEm = LocalDateTime.now();
        this.arquivado = false;
    }

    public Lote(LoteId id, FotografoId fotografoId, SessaoId sessaoId, SpotId spotId,
                String descricao, LocalDateTime criadoEm, boolean arquivado) {
        notNull(id, "O id do Lote nao pode ser nulo");
        this.id = id;
        this.fotografoId = validarFotografo(fotografoId);
        this.sessaoId = validarSessao(sessaoId);
        this.spotId = validarSpot(spotId);
        setDescricao(descricao);
        notNull(criadoEm, "A data de criacao do Lote nao pode ser nula");
        this.criadoEm = criadoEm;
        this.arquivado = arquivado;
    }

    private FotografoId validarFotografo(FotografoId id) {
        notNull(id, "O Lote precisa de um Fotografo valido");
        return id;
    }

    private SessaoId validarSessao(SessaoId id) {
        notNull(id, "O Lote precisa de uma Sessao valida");
        return id;
    }

    private SpotId validarSpot(SpotId id) {
        notNull(id, "O Lote precisa de um Spot valido");
        return id;
    }

    public LoteId getId() {
        return id;
    }

    public FotografoId getFotografoId() {
        return fotografoId;
    }

    public SessaoId getSessaoId() {
        return sessaoId;
    }

    public SpotId getSpotId() {
        return spotId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        notNull(descricao, "A descricao do Lote nao pode ser nula");
        notBlank(descricao, "A descricao do Lote nao pode estar em branco");
        this.descricao = descricao;
    }

    public boolean isArquivado() {
        return arquivado;
    }

    public void arquivar() {
        if (arquivado) {
            throw new IllegalStateException("O Lote ja esta arquivado");
        }
        this.arquivado = true;
    }
}

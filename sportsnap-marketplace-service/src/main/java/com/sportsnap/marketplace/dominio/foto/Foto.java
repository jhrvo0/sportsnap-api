package com.sportsnap.marketplace.dominio.foto;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.isTrue;

import com.sportsnap.marketplace.dominio.licenca.Dinheiro;
import com.sportsnap.marketplace.dominio.lote.LoteId;

import java.math.BigDecimal;

public class Foto {

    private static final Dinheiro PRECO_PADRAO = Dinheiro.de("29.90");

    private final FotoId id;
    private final LoteId loteId;
    private String urlPreview;
    private String urlOriginal;
    private MetadadosExif exif;
    private boolean licenciada;
    private boolean removida;
    private Dinheiro preco;
    private boolean disponivel;

    public Foto(LoteId loteId, String urlPreview, String urlOriginal, MetadadosExif exif) {
        id = null;
        notNull(loteId, "A Foto precisa de um Lote valido");
        this.loteId = loteId;
        setUrlPreview(urlPreview);
        setUrlOriginal(urlOriginal);
        setExif(exif);
        this.licenciada = false;
        this.removida = false;
        this.preco = PRECO_PADRAO;
        this.disponivel = true;
    }

    public Foto(FotoId id, LoteId loteId, String urlPreview, String urlOriginal,
                MetadadosExif exif, boolean licenciada, boolean removida,
                Dinheiro preco, boolean disponivel) {
        notNull(id, "O id da Foto nao pode ser nulo");
        notNull(loteId, "A Foto precisa de um Lote valido");
        this.id = id;
        this.loteId = loteId;
        setUrlPreview(urlPreview);
        setUrlOriginal(urlOriginal);
        setExif(exif);
        this.licenciada = licenciada;
        this.removida = removida;
        this.preco = preco != null ? preco : PRECO_PADRAO;
        this.disponivel = disponivel;
    }

    // Construtor de compatibilidade para código existente
    public Foto(FotoId id, LoteId loteId, String urlPreview, String urlOriginal,
                MetadadosExif exif, boolean licenciada, boolean removida) {
        this(id, loteId, urlPreview, urlOriginal, exif, licenciada, removida, PRECO_PADRAO, true);
    }

    public FotoId getId() { return id; }
    public LoteId getLoteId() { return loteId; }

    public String getUrlPreview() { return urlPreview; }
    public void setUrlPreview(String urlPreview) {
        notNull(urlPreview, "A URL de preview nao pode ser nula");
        notBlank(urlPreview, "A URL de preview nao pode estar em branco");
        this.urlPreview = urlPreview;
    }

    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) {
        notNull(urlOriginal, "A URL original nao pode ser nula");
        notBlank(urlOriginal, "A URL original nao pode estar em branco");
        this.urlOriginal = urlOriginal;
    }

    public MetadadosExif getExif() { return exif; }
    public void setExif(MetadadosExif exif) {
        notNull(exif, "Os metadados EXIF nao podem ser nulos");
        this.exif = exif;
    }

    public boolean isLicenciada() { return licenciada; }
    public void marcarLicenciada() { this.licenciada = true; }

    public boolean isRemovida() { return removida; }
    public void remover() {
        if (licenciada) {
            throw new IllegalStateException("Nao e possivel remover Foto ja licenciada");
        }
        this.removida = true;
    }

    public Dinheiro getPreco() { return preco; }
    public void definirPreco(BigDecimal novoPreco) {
        notNull(novoPreco, "O preco nao pode ser nulo");
        isTrue(novoPreco.compareTo(BigDecimal.ZERO) > 0, "O preco deve ser maior que zero");
        this.preco = new Dinheiro(novoPreco);
    }

    public boolean isDisponivel() { return disponivel; }
    public void disponibilizar() {
        if (removida) throw new IllegalStateException("Foto removida nao pode ser disponibilizada");
        this.disponivel = true;
    }
    public void indisponibilizar() {
        if (licenciada) throw new IllegalStateException("Foto licenciada nao pode ser indisponibilizada");
        this.disponivel = false;
    }
}

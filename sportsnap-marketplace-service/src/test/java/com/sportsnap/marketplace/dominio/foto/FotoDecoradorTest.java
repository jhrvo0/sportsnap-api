package com.sportsnap.marketplace.dominio.foto;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.marketplace.dominio.licenca.Dinheiro;
import com.sportsnap.marketplace.dominio.lote.LoteId;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class FotoDecoradorTest {

    private Foto criarFotoBase() {
        var loteId = new LoteId(1);
        var foto = new Foto(loteId, "/preview.jpg", "/original.jpg", new MetadadosExif(LocalDateTime.now(), "Canon"));
        return foto;
    }

    @Test
    void previewBasicoRetornaUrlEDescricao() {
        var foto = criarFotoBase();
        var preview = new FotoPreviewBasico(foto);

        assertEquals("/preview.jpg", preview.getUrlPreview());
        assertEquals(foto.getExif().getDetalhes(), preview.getDescricaoPreview());
    }

    @Test
    void marcaDaguaAdicionaParametroAUrl() {
        var foto = criarFotoBase();
        var preview = new FotoPreviewBasico(foto);
        var comMarca = new FotoComMarcaDagua(preview);

        assertTrue(comMarca.getUrlPreview().contains("?marca="));
        assertTrue(comMarca.getUrlPreview().contains("©+SportSnap"));
    }

    @Test
    void marcaDaguaAdicionaSufixoADescricao() {
        var foto = criarFotoBase();
        var preview = new FotoPreviewBasico(foto);
        var comMarca = new FotoComMarcaDagua(preview);

        assertTrue(comMarca.getDescricaoPreview().contains("[© SportSnap]"));
    }

    @Test
    void empilharDecoradores() {
        var foto = criarFotoBase();
        var preview = new FotoPreviewBasico(foto);
        var comMarca = new FotoComMarcaDagua(preview);
        var empilhado = new FotoComMarcaDagua(comMarca);

        assertTrue(empilhado.getUrlPreview().contains("?marca="));
        assertTrue(empilhado.getDescricaoPreview().contains("[© SportSnap]"));
        assertTrue(empilhado.getDescricaoPreview().contains("[© SportSnap]"));
    }
}

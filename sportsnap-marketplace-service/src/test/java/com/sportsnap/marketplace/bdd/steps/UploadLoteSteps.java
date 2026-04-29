package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.domain.entities.*;
import com.sportsnap.marketplace.domain.repositories.*;
import com.sportsnap.marketplace.domain.usecases.UploadIndexacaoEmLote;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UploadLoteSteps {

    @Autowired
    private FotografoRepository fotografoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private LicencaDeImagemRepository licencaRepository;

    @Autowired
    private SplitFinanceiroRepository splitRepository;

    @Autowired
    private UploadIndexacaoEmLote uploadIndexacaoEmLote;

    private Fotografo fotografo;
    private Lote lote;

    @Before
    public void setUp() {
        splitRepository.deleteAll();
        licencaRepository.deleteAll();
        fotoRepository.deleteAll();
        loteRepository.deleteAll();
        fotografoRepository.deleteAll();
    }

    @Dado("que o Fotografo {string} possui um Lote cadastrado")
    public void fotografoPossuiLoteCadastrado(String nome) {
        fotografo = new Fotografo(nome, nome.toLowerCase() + "@foto.com");
        fotografo = fotografoRepository.save(fotografo);

        lote = new Lote(100L, 200L, fotografo);
        lote = loteRepository.save(lote);
    }

    @Quando("o Fotografo faz upload de {int} fotos no Lote")
    public void fotografoFazUpload(int quantidade) {
        List<String> caminhos = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            caminhos.add("/fotos/evento/foto_" + i + ".jpg");
        }
        uploadIndexacaoEmLote.executar(lote.getId(), caminhos);
    }

    @Quando("o Fotografo tenta fazer upload de {int} fotos no Lote")
    public void fotografoTentaFazUpload(int quantidade) {
        List<String> caminhos = new ArrayList<>();
        for (int i = 1; i <= quantidade; i++) {
            caminhos.add("/fotos/evento/foto_" + i + ".jpg");
        }
        uploadIndexacaoEmLote.executar(lote.getId(), caminhos);
    }

    @Entao("as {int} fotos sao registradas no Lote")
    public void fotosRegistradasNoLote(int quantidade) {
        List<Foto> fotos = fotoRepository.findByLoteId(lote.getId());
        assertEquals(quantidade, fotos.size(),
                "Deveria ter " + quantidade + " fotos registradas no lote");
    }

    @E("cada foto possui metadados EXIF extraidos")
    public void cadaFotoPossuiMetadadosExif() {
        List<Foto> fotos = fotoRepository.findByLoteId(lote.getId());
        for (Foto foto : fotos) {
            assertNotNull(foto.getTimestampExif(),
                    "Cada foto deve possuir timestamp EXIF extraido");
            assertNotNull(foto.getMetadadosExif(),
                    "Cada foto deve possuir metadados EXIF extraidos");
        }
    }

    @Entao("a foto possui timestamp EXIF preenchido")
    public void fotoPossuiTimestampExif() {
        List<Foto> fotos = fotoRepository.findByLoteId(lote.getId());
        assertFalse(fotos.isEmpty(), "Deveria ter pelo menos uma foto");
        Foto foto = fotos.get(0);
        assertNotNull(foto.getTimestampExif(), "Foto deve ter timestamp EXIF preenchido");
    }

    @E("a foto possui URL de preview gerada")
    public void fotoPossuiUrlPreview() {
        List<Foto> fotos = fotoRepository.findByLoteId(lote.getId());
        assertFalse(fotos.isEmpty(), "Deveria ter pelo menos uma foto");
        Foto foto = fotos.get(0);
        assertNotNull(foto.getUrlPreview(), "Foto deve ter URL de preview gerada");
        assertTrue(foto.getUrlPreview().startsWith("preview_"),
                "URL de preview deve comecar com 'preview_'");
    }

    @Entao("nenhuma foto e adicionada ao Lote")
    public void nenhumaFotoAdicionada() {
        List<Foto> fotos = fotoRepository.findByLoteId(lote.getId());
        assertEquals(0, fotos.size(), "Nenhuma foto deveria ter sido adicionada ao lote");
    }
}

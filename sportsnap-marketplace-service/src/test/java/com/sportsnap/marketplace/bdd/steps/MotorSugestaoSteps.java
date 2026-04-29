package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteServico;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;
import com.sportsnap.marketplace.dominio.sugestao.JanelaCheckIn;
import com.sportsnap.marketplace.dominio.sugestao.MotorSugestaoServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MotorSugestaoSteps {

    @Autowired private FotografoServico fotografoServico;
    @Autowired private LoteServico loteServico;
    @Autowired private FotoServico fotoServico;
    @Autowired private VendaServico vendaServico;
    @Autowired private MotorSugestaoServico motorSugestaoServico;

    private Foto foto;
    private List<Foto> sugestoes;
    private List<JanelaCheckIn> janelas;
    private boolean fotoDentroJanela;

    @Dado("que existe uma Foto cujo EXIF cai dentro da janela do Atleta {int}")
    public void fotoDentroJanela(Integer atletaId) {
        fotoDentroJanela = true;
        var fotografo = fotografoServico.cadastrar("FotoSug", new Email("sug@foto.com"));
        var lote = loteServico.cadastrar(fotografo.getId(), new SessaoId(1), new SpotId(1),
            "Lote Sug");
        var fotos = fotoServico.uploadEmLote(lote.getId(), List.of("/fotos/sug.jpg"));
        foto = fotos.get(0);
    }

    @Dado("que existe uma Foto cujo EXIF esta fora da janela do Atleta {int}")
    public void fotoForaJanela(Integer atletaId) {
        fotoDentroJanela = false;
        var fotografo = fotografoServico.cadastrar("FotoForaJanela", new Email("forajanela@foto.com"));
        var lote = loteServico.cadastrar(fotografo.getId(), new SessaoId(2), new SpotId(2),
            "Lote fora janela");
        var fotos = fotoServico.uploadEmLote(lote.getId(), List.of("/fotos/fora.jpg"));
        foto = fotos.get(0);
    }

    @E("o Atleta {int} ja adquiriu essa Foto")
    public void atletaJaAdquiriuEssaFoto(Integer atletaId) {
        vendaServico.processarVenda(new AtletaId(atletaId), foto.getId());
    }

    @Quando("o Atleta {int} solicita sugestoes para sua janela")
    public void atletaSolicitaSugestoes(Integer atletaId) {
        var timestamp = foto.getExif().getTimestamp();
        var janela = fotoDentroJanela
            ? new JanelaCheckIn(timestamp.minusMinutes(30), timestamp.plusMinutes(30))
            : new JanelaCheckIn(timestamp.plusDays(1), timestamp.plusDays(1).plusHours(1));
        janelas = List.of(janela);
        sugestoes = motorSugestaoServico.sugerirParaAtleta(new AtletaId(atletaId), janelas);
    }

    @Quando("o Atleta {int} solicita sugestoes sem janelas")
    public void atletaSolicitaSemJanelas(Integer atletaId) {
        sugestoes = motorSugestaoServico.sugerirParaAtleta(new AtletaId(atletaId), new ArrayList<>());
    }

    @Entao("recebo {int} foto sugerida")
    public void receboFotoSugerida(Integer quantidade) {
        assertEquals(quantidade, sugestoes.size());
    }

    @Entao("recebo {int} fotos sugeridas")
    public void receboFotosSugeridas(Integer quantidade) {
        assertEquals(quantidade, sugestoes.size());
    }

    @Quando("o Atleta {int} favorita a Foto")
    public void atletaFavorita(Integer atletaId) {
        motorSugestaoServico.favoritar(new AtletaId(atletaId), foto.getId());
    }

    @Entao("a Foto aparece nos favoritos do Atleta {int}")
    public void fotoAparaceNosFavoritos(Integer atletaId) {
        var favoritos = motorSugestaoServico.listarFavoritos(new AtletaId(atletaId));
        assertTrue(favoritos.stream().anyMatch(id -> id.equals(foto.getId())));
    }

    @Quando("o Atleta {int} desfavorita a Foto")
    public void atletaDesfavorita(Integer atletaId) {
        motorSugestaoServico.desfavoritar(new AtletaId(atletaId), foto.getId());
    }

    @Entao("a Foto nao aparece mais nos favoritos do Atleta {int}")
    public void fotoNaoApareceNosFavoritos(Integer atletaId) {
        var favoritos = motorSugestaoServico.listarFavoritos(new AtletaId(atletaId));
        assertFalse(favoritos.stream().anyMatch(id -> id.equals(foto.getId())));
    }
}

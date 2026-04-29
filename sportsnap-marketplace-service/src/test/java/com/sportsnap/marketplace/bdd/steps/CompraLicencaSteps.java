package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;
import com.sportsnap.marketplace.dominio.licenca.VendaServico.LicencaAdquiridaEvento;
import com.sportsnap.marketplace.dominio.licenca.VendaServico.LicencaCanceladaEvento;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteServico;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompraLicencaSteps {

    @Autowired private FotografoServico fotografoServico;
    @Autowired private LoteServico loteServico;
    @Autowired private FotoServico fotoServico;
    @Autowired private VendaServico vendaServico;
    @Autowired private LicencaRepositorio licencaRepositorio;
    @Autowired private ColetorDeEventos coletorDeEventos;

    private Fotografo fotografo;
    private Lote lote;
    private Foto foto;
    private LicencaDeImagem licencaAtual;
    private Exception excecao;
    private com.sportsnap.marketplace.dominio.licenca.Dinheiro totalGasto;

    @Dado("que existe uma Foto disponivel")
    public void existeFotoDisponivel() {
        fotografo = fotografoServico.cadastrar("FotografoVenda",
            new Email("venda@foto.com"));
        lote = loteServico.cadastrar(fotografo.getId(), new SessaoId(1), new SpotId(1),
            "Lote Venda");
        var fotos = fotoServico.uploadEmLote(lote.getId(), List.of("/fotos/venda.jpg"));
        foto = fotos.get(0);
    }

    @Quando("o Atleta {int} adquire a Licenca da Foto")
    public void atletaAdquireLicenca(Integer atletaId) {
        licencaAtual = vendaServico.processarVenda(new AtletaId(atletaId), foto.getId());
    }

    @E("o Atleta {int} ja adquiriu a Licenca da Foto")
    public void atletaJaAdquiriuLicenca(Integer atletaId) {
        licencaAtual = vendaServico.processarVenda(new AtletaId(atletaId), foto.getId());
    }

    @Quando("o Atleta {int} tenta adquirir a Licenca da Foto novamente")
    public void atletaTentaAdquirirNovamente(Integer atletaId) {
        try {
            vendaServico.processarVenda(new AtletaId(atletaId), foto.getId());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Atleta {int} tenta adquirir a Licenca da Foto")
    public void atletaTentaAdquirir(Integer atletaId) {
        try {
            vendaServico.processarVenda(new AtletaId(atletaId), foto.getId());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("a Licenca e registrada com preco {string}")
    public void licencaRegistradaComPreco(String preco) {
        assertNotNull(licencaAtual);
        assertEquals(new BigDecimal(preco), licencaAtual.getPreco().getValor());
    }

    @E("um SplitFinanceiro e criado atomicamente")
    public void splitCriadoAtomicamente() {
        var split = vendaServico.obterSplit(licencaAtual.getId());
        assertNotNull(split);
    }

    @E("um evento LicencaAdquiridaEvento e publicado")
    public void eventoLicencaAdquirida() {
        assertTrue(coletorDeEventos.getEventos().stream()
            .anyMatch(e -> e instanceof LicencaAdquiridaEvento));
    }

    @Entao("o valor do Fotografo no Split e {string}")
    public void valorFotografoSplit(String esperado) {
        var split = vendaServico.obterSplit(licencaAtual.getId());
        assertEquals(new BigDecimal(esperado), split.getValorFotografo().getValor());
    }

    @E("a taxa da plataforma no Split e {string}")
    public void taxaPlataformaSplit(String esperado) {
        var split = vendaServico.obterSplit(licencaAtual.getId());
        assertEquals(new BigDecimal(esperado), split.getTaxaPlataforma().getValor());
    }

    @Entao("a compra e rejeitada")
    public void compraRejeitada() {
        assertNotNull(excecao, "Compra deveria ter falhado");
    }

    @Entao("existem {int} Licencas para a Foto")
    public void existemLicencasParaFoto(Integer quantidade) {
        assertEquals(quantidade, licencaRepositorio.listarPorFoto(foto.getId()).size());
    }

    @Quando("consulto as Licencas do Atleta {int}")
    public void consultoLicencasAtleta(Integer atletaId) {
        licencaAtual = vendaServico.listarPorAtleta(new AtletaId(atletaId)).get(0);
    }

    @Entao("recebo {int} Licenca")
    public void receboLicencas(Integer quantidade) {
        assertEquals(quantidade, vendaServico.listarPorAtleta(licencaAtual.getAtletaId()).size());
    }

    @Quando("cancelo a ultima Licenca do Atleta {int}")
    public void canceloUltimaLicenca(Integer atletaId) {
        var licencas = vendaServico.listarPorAtleta(new AtletaId(atletaId));
        var licencaAtiva = licencas.stream().filter(l -> !l.isCancelada()).findFirst().orElseThrow();
        vendaServico.cancelar(licencaAtiva.getId());
        licencaAtual = licencaAtiva;
    }

    @Entao("a Licenca fica marcada como cancelada")
    public void licencaCancelada() {
        var recuperada = vendaServico.obter(licencaAtual.getId());
        assertTrue(recuperada.isCancelada());
    }

    @E("um evento LicencaCanceladaEvento e publicado")
    public void eventoLicencaCancelada() {
        assertTrue(coletorDeEventos.getEventos().stream()
            .anyMatch(e -> e instanceof LicencaCanceladaEvento));
    }

    @E("a Foto foi removida pelo Fotografo")
    public void fotoRemovida() {
        fotoServico.remover(foto.getId());
    }

    @E("consulto o total gasto pelo Atleta {int}")
    public void consultoTotalGasto(Integer atletaId) {
        totalGasto = vendaServico.calcularTotalGastoPeloAtleta(new AtletaId(atletaId));
    }

    @Entao("o total gasto e {string}")
    public void totalGastoE(String esperado) {
        assertEquals(new BigDecimal(esperado), totalGasto.getValor());
    }
}

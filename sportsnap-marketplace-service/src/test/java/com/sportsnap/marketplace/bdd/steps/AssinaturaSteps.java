package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.dominio.assinatura.AssinaturaRepositorio;
import com.sportsnap.marketplace.dominio.assinatura.AssinaturaServico;
import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.MetadadosExif;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteId;
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

public class AssinaturaSteps {

    @Autowired private AssinaturaServico assinaturaServico;
    @Autowired private AssinaturaRepositorio assinaturaRepositorio;
    @Autowired private VendaServico vendaServico;
    @Autowired private LicencaRepositorio licencaRepositorio;
    @Autowired private SplitRepositorio splitRepositorio;
    @Autowired private FotoRepositorio fotoRepositorio;
    @Autowired private FotografoServico fotografoServico;
    @Autowired private LoteServico loteServico;

    private AtletaId atletaAtual;
    private Exception excecao;
    private Foto fotoAtual;
    private LicencaDeImagem licencaAtual;

    @Dado("que o Atleta {int} ja possui assinatura ativa")
    public void atletaPossuiAssinaturaAtiva(Integer atletaId) {
        atletaAtual = new AtletaId(atletaId);
        assinaturaServico.assinar(atletaAtual);
    }

    @Quando("o Atleta {int} assina o plano mensal")
    public void atletaAssinaPlanoMensal(Integer atletaId) {
        atletaAtual = new AtletaId(atletaId);
        assinaturaServico.assinar(atletaAtual);
    }

    @Quando("o Atleta {int} tenta assinar novamente")
    public void atletaTentaAssinarNovamente(Integer atletaId) {
        atletaAtual = new AtletaId(atletaId);
        try {
            assinaturaServico.assinar(atletaAtual);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("a assinatura e criada com status {string}")
    public void assinaturaCriadaComStatus(String status) {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        assertEquals(status, assinatura.getStatus().name());
    }

    @Entao("o atleta possui {int} cotas disponiveis")
    public void atletaPossuiCotasDisponiveis(Integer cotasEsperadas) {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        assertEquals(cotasEsperadas, assinatura.getSaldoCotas());
    }

    @Entao("a assinatura e rejeitada")
    public void assinaturaRejeitada() {
        assertNotNull(excecao, "Assinatura deveria ter falhado");
    }

    @Dado("que existe uma Foto disponivel para assinatura")
    public void existeFotoDisponivel() {
        var fotografo = fotografoServico.cadastrar("FotografoAssinatura", new Email("assinatura@foto.com"));
        var lote = loteServico.cadastrar(fotografo.getId(), new SessaoId(1), new SpotId(1), "Lote Assinatura");
        var fotos = fotoRepositorio.listarPorLote(lote.getId());
        if (fotos.isEmpty()) {
            fotoAtual = new Foto(lote.getId(), "/preview.jpg", "/original.jpg", new MetadadosExif(java.time.LocalDateTime.now(), "Canon"));
            fotoAtual = fotoRepositorio.salvar(fotoAtual);
        } else {
            fotoAtual = fotos.get(0);
        }
    }

    @Quando("o Atleta {int} adquire a Licenca da Foto via cota")
    public void atletaAdquireLicencaViaCota(Integer atletaId) {
        licencaAtual = vendaServico.processarVenda(new AtletaId(atletaId), fotoAtual.getId());
    }

    @E("o Atleta {int} adquiriu a Licenca da Foto via cota")
    public void atletaAdquiriuLicencaViaCota(Integer atletaId) {
        licencaAtual = vendaServico.processarVenda(new AtletaId(atletaId), fotoAtual.getId());
    }

    @Entao("a licenca e registrada com preco {string}")
    public void licencaRegistradaComPreco(String preco) {
        assertNotNull(licencaAtual);
        assertEquals(new BigDecimal(preco), licencaAtual.getPreco().getValor());
    }

    @Entao("a licenca esta marcada como adquirida via cota")
    public void licencaMarcadaComoViaCota() {
        assertTrue(licencaAtual.isAdquiridaViaCota());
    }

    @Entao("o saldo de cotas do atleta e reduzido em {int}")
    public void saldoCotasReduzido(Integer reducao) {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        assertEquals(10 - reducao, assinatura.getSaldoCotas());
    }

    @Quando("o Atleta {int} cancela a assinatura")
    public void atletaCancelaAssinatura(Integer atletaId) {
        assinaturaServico.cancelar(new AtletaId(atletaId));
    }

    @Entao("o status da assinatura e {string}")
    public void statusAssinatura(String status) {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        assertEquals(status, assinatura.getStatus().name());
    }

    @Entao("o atleta ainda possui cotas disponiveis")
    public void atletaAindaPossuiCotas() {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        assertTrue(assinatura.getSaldoCotas() > 0);
    }

    @Quando("o ciclo de assinatura do Atleta {int} e fechado")
    public void cicloAssinaturaFechado(Integer atletaId) {
        assinaturaServico.fecharCicloEProcessarRateio(new AtletaId(atletaId));
    }

    @Entao("um SplitFinanceiro e gerado para a licenca")
    public void splitFinanceiroGerado() {
        var split = splitRepositorio.obterPorLicenca(licencaAtual.getId());
        assertTrue(split.isPresent());
    }

    @Entao("o valor do fotografo no split e calculado com base na mensalidade")
    public void valorFotografoSplitCalculado() {
        var split = splitRepositorio.obterPorLicenca(licencaAtual.getId()).orElseThrow();
        var poolTotal = AssinaturaServico.VALOR_MENSALIDADE.multiplicar(AssinaturaServico.PERCENTUAL_FOTOGRAFO);
        assertEquals(poolTotal.getValor(), split.getValorFotografo().getValor());
    }

    @Dado("o atleta ajusta saldo para {int} cotas")
    public void atletaPossuiCotas(Integer cotas) {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        while (assinatura.getSaldoCotas() < cotas) {
            assinaturaServico.restituirCota(atletaAtual);
        }
    }

    @Entao("o saldo de cotas nao excede {int}")
    public void saldoCotasNaoExcede(Integer limite) {
        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaAtual).orElseThrow();
        assertTrue(assinatura.getSaldoCotas() <= limite);
    }
}

package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.dashboard.DashboardServico;
import com.sportsnap.marketplace.dominio.dashboard.ResumoFotografo;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;
import com.sportsnap.marketplace.dominio.lote.LoteServico;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardFotografoSteps {

    @Autowired private FotografoServico fotografoServico;
    @Autowired private LoteServico loteServico;
    @Autowired private FotoServico fotoServico;
    @Autowired private VendaServico vendaServico;
    @Autowired private DashboardServico dashboardServico;

    private final Map<String, Fotografo> fotografosPorNome = new HashMap<>();
    private Fotografo fotografoAtual;
    private ResumoFotografo resumo;
    private com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem licencaAtual;
    private Exception excecao;

    @Dado("que existe o Fotografo {string} cadastrado com {int} Lotes e {int} fotos por Lote")
    public void fotografoComLotesEFotos(String nome, Integer lotes, Integer fotosPorLote) {
        cadastrarFotografo(nome);
        for (int i = 0; i < lotes; i++) {
            var lote = loteServico.cadastrar(fotografoAtual.getId(),
                new SessaoId(i + 1), new SpotId(i + 1), "Lote " + (i + 1));
            var caminhos = new ArrayList<String>();
            for (int j = 0; j < fotosPorLote; j++) {
                caminhos.add("/fotos/" + nome + "_" + i + "_" + j + ".jpg");
            }
            fotoServico.uploadEmLote(lote.getId(), caminhos);
        }
    }

    @Dado("que existe o Fotografo {string} cadastrado com {int} Lote e {int} foto vendida")
    public void fotografoComLoteEFotoVendida(String nome, Integer lotes, Integer vendidas) {
        cadastrarFotografo(nome);
        var lote = loteServico.cadastrar(fotografoAtual.getId(), new SessaoId(1), new SpotId(1),
            "Lote principal");
        var caminhos = new ArrayList<String>();
        for (int i = 0; i < vendidas; i++) {
            caminhos.add("/fotos/" + nome + "_" + i + ".jpg");
        }
        var fotos = fotoServico.uploadEmLote(lote.getId(), caminhos);
        for (int i = 0; i < vendidas; i++) {
            licencaAtual = vendaServico.processarVenda(new AtletaId(100 + i), fotos.get(i).getId());
        }
    }

    @Dado("que existe o Fotografo {string} cadastrado com {int} Lote e {int} foto")
    public void fotografoComLoteEFoto(String nome, Integer lotes, Integer fotos) {
        cadastrarFotografo(nome);
        var lote = loteServico.cadastrar(fotografoAtual.getId(), new SessaoId(1), new SpotId(1),
            "Lote principal");
        var caminhos = new ArrayList<String>();
        for (int i = 0; i < fotos; i++) {
            caminhos.add("/fotos/" + nome + "_" + i + ".jpg");
        }
        fotoServico.uploadEmLote(lote.getId(), caminhos);
    }

    @E("a licenca e cancelada dentro da janela")
    public void licencaCancelada() {
        vendaServico.cancelar(licencaAtual.getId());
    }

    @Quando("consulto o resumo do Fotografo {string}")
    public void consultoResumo(String nome) {
        var fotografo = fotografosPorNome.get(nome);
        resumo = dashboardServico.consultarResumo(fotografo.getId());
    }

    @Quando("tento consultar o resumo de um Fotografo inexistente")
    public void tentoConsultarFotografoInexistente() {
        try {
            dashboardServico.consultarResumo(new FotografoId(9999));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o resumo exibe {int} Lotes")
    public void resumoExibeLotes(Integer quantidade) {
        assertEquals(quantidade, resumo.getTotalLotes());
    }

    @E("o resumo exibe {int} fotos")
    public void resumoExibeFotos(Integer quantidade) {
        assertEquals(quantidade, resumo.getTotalFotos());
    }

    @Entao("o resumo exibe {int} venda")
    public void resumoExibeVenda(Integer quantidade) {
        assertEquals(quantidade, resumo.getTotalVendas());
    }

    @Entao("o resumo exibe {int} vendas")
    public void resumoExibeVendas(Integer quantidade) {
        assertEquals(quantidade, resumo.getTotalVendas());
    }

    @E("o saldo disponivel do Fotografo e {string}")
    public void saldoDisponivel(String valorEsperado) {
        assertEquals(new java.math.BigDecimal(valorEsperado),
            resumo.getSaldoDisponivel().getValor());
    }

    @Entao("a consulta e rejeitada")
    public void consultaRejeitada() {
        assertNotNull(excecao);
    }

    private void cadastrarFotografo(String nome) {
        fotografoAtual = fotografoServico.cadastrar(nome,
            new Email(nome.toLowerCase() + "@foto.com"));
        fotografosPorNome.put(nome, fotografoAtual);
    }
}

package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;
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

public class GerenciarLoteSteps {

    @Autowired private FotografoServico fotografoServico;
    @Autowired private LoteServico loteServico;
    @Autowired private FotoServico fotoServico;
    @Autowired private FotoRepositorio fotoRepositorio;
    @Autowired private LoteRepositorio loteRepositorio;

    private final Map<String, Fotografo> fotografosPorNome = new HashMap<>();
    private Fotografo fotografoAtual;
    private Lote loteAtual;
    private List<Lote> lotesConsultados;
    private Exception excecao;

    @Dado("que existe o Fotografo {string} cadastrado")
    public void fotografoCadastrado(String nome) {
        var fotografo = fotografoServico.cadastrar(nome,
            new Email(nome.toLowerCase() + "@foto.com"));
        fotografosPorNome.put(nome, fotografo);
        fotografoAtual = fotografo;
    }

    @Quando("cadastro um Lote para a Sessao {int} e Spot {int} com descricao {string}")
    public void cadastroLote(Integer sessao, Integer spot, String descricao) {
        loteAtual = loteServico.cadastrar(fotografoAtual.getId(),
            new SessaoId(sessao), new SpotId(spot), descricao);
    }

    @Entao("o Lote e salvo com id")
    public void loteSalvoComId() {
        assertNotNull(loteAtual);
        assertNotNull(loteAtual.getId());
    }

    @Quando("tento cadastrar um Lote para Fotografo inexistente")
    public void tentoCadastrarLoteFotografoInexistente() {
        try {
            loteServico.cadastrar(new FotografoId(999), new SessaoId(1), new SpotId(1), "Invalido");
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o cadastro do Lote e rejeitado")
    public void cadastroLoteRejeitado() {
        assertNotNull(excecao, "Deveria ter lancado excecao");
    }

    @E("existe um Lote do Fotografo {string}")
    public void existeLoteDoFotografo(String nome) {
        var fotografo = fotografosPorNome.get(nome);
        loteAtual = loteServico.cadastrar(fotografo.getId(),
            new SessaoId(1), new SpotId(1), "Lote de " + nome);
    }

    @E("existe um Lote do Fotografo {string} com {int} foto")
    public void existeLoteComFoto(String nome, Integer quantidade) {
        existeLoteDoFotografo(nome);
        List<String> caminhos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            caminhos.add("/fotos/foto_" + (i + 1) + ".jpg");
        }
        fotoServico.uploadEmLote(loteAtual.getId(), caminhos);
    }

    @Quando("o Fotografo faz upload de {int} fotos no Lote")
    public void fotografoFazUpload(Integer quantidade) {
        List<String> caminhos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            caminhos.add("/fotos/foto_" + (i + 1) + ".jpg");
        }
        fotoServico.uploadEmLote(loteAtual.getId(), caminhos);
    }

    @Quando("o Fotografo tenta fazer upload de {int} fotos no Lote")
    public void fotografoTentaFazUpload(Integer quantidade) {
        try {
            List<String> caminhos = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) {
                caminhos.add("/fotos/foto_" + (i + 1) + ".jpg");
            }
            fotoServico.uploadEmLote(loteAtual.getId(), caminhos);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("{int} fotos sao registradas no Lote")
    public void fotosRegistradas(Integer quantidade) {
        var fotos = fotoServico.listarPorLote(loteAtual.getId());
        assertEquals(quantidade, fotos.size());
    }

    @E("cada foto possui metadados EXIF extraidos")
    public void cadaFotoPossuiExif() {
        var fotos = fotoServico.listarPorLote(loteAtual.getId());
        for (Foto foto : fotos) {
            assertNotNull(foto.getExif());
            assertNotNull(foto.getExif().getTimestamp());
        }
    }

    @Entao("o upload e rejeitado")
    public void uploadRejeitado() {
        assertNotNull(excecao, "Upload deveria ter falhado");
    }

    @E("o Fotografo {string} possui {int} Lotes cadastrados")
    public void fotografoPossuiLotes(String nome, Integer quantidade) {
        var fotografo = fotografosPorNome.get(nome);
        for (int i = 0; i < quantidade; i++) {
            loteServico.cadastrar(fotografo.getId(), new SessaoId(i + 1), new SpotId(i + 1),
                "Lote " + (i + 1));
        }
    }

    @Quando("consulto os Lotes do Fotografo {string}")
    public void consultoLotes(String nome) {
        var fotografo = fotografosPorNome.get(nome);
        lotesConsultados = loteServico.listarPorFotografo(fotografo.getId());
    }

    @Entao("recebo {int} Lotes")
    public void receboLotes(Integer quantidade) {
        assertEquals(quantidade, lotesConsultados.size());
    }

    @Quando("edito a descricao do Lote para {string}")
    public void editoDescricaoLote(String novaDescricao) {
        loteServico.editarDescricao(loteAtual.getId(), novaDescricao);
    }

    @Entao("o Lote tem a descricao atualizada")
    public void loteDescricaoAtualizada() {
        var recuperado = loteServico.obter(loteAtual.getId());
        assertEquals("Novo titulo", recuperado.getDescricao());
    }

    @Quando("arquivo o Lote")
    public void arquivoLote() {
        loteServico.arquivar(loteAtual.getId());
    }

    @Quando("removo a primeira foto do Lote")
    public void removoPrimeiraFoto() {
        var fotos = fotoServico.listarPorLote(loteAtual.getId());
        fotoServico.remover(fotos.get(0).getId());
    }

    @Entao("a foto fica marcada como removida")
    public void fotoMarcadaRemovida() {
        var fotos = loteRepositorio.obter(loteAtual.getId()).orElseThrow();
        var todas = fotoRepositorio.listarPorLote(fotos.getId());
        assertTrue(todas.isEmpty(), "Lista ativa deve estar vazia apos remocao");
    }
}

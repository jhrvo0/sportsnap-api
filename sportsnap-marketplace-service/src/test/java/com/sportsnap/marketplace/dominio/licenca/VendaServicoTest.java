package com.sportsnap.marketplace.dominio.licenca;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.evento.EventoBarramento;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.MetadadosExif;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.dominio.assinatura.AssinaturaServico;
import com.sportsnap.marketplace.infraestrutura.memoria.AssinaturaRepositorioEmMemoria;
import com.sportsnap.marketplace.infraestrutura.memoria.FotoRepositorioMemoria;
import com.sportsnap.marketplace.infraestrutura.memoria.LicencaRepositorioMemoria;
import com.sportsnap.marketplace.infraestrutura.memoria.SplitRepositorioMemoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VendaServicoTest {

    private final LicencaRepositorio licencaRepositorio = new LicencaRepositorioMemoria();
    private final SplitRepositorio splitRepositorio = new SplitRepositorioMemoria();
    private final FotoRepositorio fotoRepositorio = new FotoRepositorioMemoria();
    private final AssinaturaRepositorioEmMemoria assinaturaRepositorio = new AssinaturaRepositorioEmMemoria();
    private final List<Object> eventosCapturados = new ArrayList<>();

    private EventoBarramento barramento;
    private AssinaturaServico assinaturaServico;
    private VendaServico vendaServico;

    @BeforeEach
    void setUp() {
        barramento = new EventoBarramento() {
            @Override
            public void postar(Object evento) {
                eventosCapturados.add(evento);
            }
        };
        assinaturaServico = new AssinaturaServico(assinaturaRepositorio, licencaRepositorio,
            splitRepositorio, fotoRepositorio);
        vendaServico = new VendaServico(licencaRepositorio, splitRepositorio, fotoRepositorio,
            barramento, assinaturaServico);
        eventosCapturados.clear();
    }

    private Foto criarFotoDisponivel() {
        var loteId = new LoteId(1);
        var foto = new Foto(loteId, "/preview.jpg", "/original.jpg", new MetadadosExif(java.time.LocalDateTime.now(), "Canon"));
        return fotoRepositorio.salvar(foto);
    }

    @Test
    void compraAvulsaComSplit() {
        var foto = criarFotoDisponivel();
        var atletaId = new AtletaId(1);

        var licenca = vendaServico.processarVenda(atletaId, foto.getId());

        assertNotNull(licenca);
        assertEquals(VendaServico.PRECO_PADRAO, licenca.getPreco());
        assertFalse(licenca.isAdquiridaViaCota());

        var split = vendaServico.obterSplit(licenca.getId());
        var valorFotografo = VendaServico.PRECO_PADRAO.multiplicar(VendaServico.PERCENTUAL_FOTOGRAFO);
        var taxaPlataforma = VendaServico.PRECO_PADRAO.multiplicar(VendaServico.PERCENTUAL_PLATAFORMA);
        assertEquals(valorFotografo, split.getValorFotografo());
        assertEquals(taxaPlataforma, split.getTaxaPlataforma());
    }

    @Test
    void compraViaCotaPrecoZero() {
        var foto = criarFotoDisponivel();
        var atletaId = new AtletaId(1);
        assinaturaServico.assinar(atletaId);

        var licenca = vendaServico.processarVenda(atletaId, foto.getId());

        assertNotNull(licenca);
        assertEquals(Dinheiro.ZERO, licenca.getPreco());
        assertTrue(licenca.isAdquiridaViaCota());
    }

    @Test
    void compraDeFotoRemovida() {
        var foto = criarFotoDisponivel();
        foto.remover();
        fotoRepositorio.salvar(foto);

        var atletaId = new AtletaId(1);
        var erro = assertThrows(IllegalStateException.class,
            () -> vendaServico.processarVenda(atletaId, foto.getId()));
        assertTrue(erro.getMessage().contains("Foto removida"));
    }

    @Test
    void compraDuplicada() {
        var foto = criarFotoDisponivel();
        var atletaId = new AtletaId(1);

        vendaServico.processarVenda(atletaId, foto.getId());

        var erro = assertThrows(IllegalStateException.class,
            () -> vendaServico.processarVenda(atletaId, foto.getId()));
        assertTrue(erro.getMessage().contains("ja possui licenca ativa"));
    }

    @Test
    void cancelarLicencaAvulsa() {
        var foto = criarFotoDisponivel();
        var atletaId = new AtletaId(1);
        var licenca = vendaServico.processarVenda(atletaId, foto.getId());

        vendaServico.cancelar(licenca.getId());

        var licencaCancelada = vendaServico.obter(licenca.getId());
        assertTrue(licencaCancelada.isCancelada());
        assertTrue(eventosCapturados.stream().anyMatch(e -> e instanceof VendaServico.LicencaCanceladaEvento));
    }

    @Test
    void cancelarLicencaViaCotaRestituiCota() {
        var foto = criarFotoDisponivel();
        var atletaId = new AtletaId(1);
        assinaturaServico.assinar(atletaId);
        int cotasAntes = assinaturaServico.obterAtiva(atletaId).getSaldoCotas();

        var licenca = vendaServico.processarVenda(atletaId, foto.getId());

        int cotasAposCompra = assinaturaServico.obterAtiva(atletaId).getSaldoCotas();
        assertEquals(cotasAntes - 1, cotasAposCompra);

        vendaServico.cancelar(licenca.getId());

        int cotasAposCancelar = assinaturaServico.obterAtiva(atletaId).getSaldoCotas();
        assertEquals(cotasAntes, cotasAposCancelar);
    }
}

package com.sportsnap.marketplace.dominio.assinatura;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.MetadadosExif;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.infraestrutura.memoria.AssinaturaRepositorioEmMemoria;
import com.sportsnap.marketplace.infraestrutura.memoria.FotoRepositorioMemoria;
import com.sportsnap.marketplace.infraestrutura.memoria.LicencaRepositorioMemoria;
import com.sportsnap.marketplace.infraestrutura.memoria.SplitRepositorioMemoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssinaturaServicoTest {

    private final AssinaturaRepositorioEmMemoria assinaturaRepositorio = new AssinaturaRepositorioEmMemoria();
    private final LicencaRepositorio licencaRepositorio = new LicencaRepositorioMemoria();
    private final SplitRepositorio splitRepositorio = new SplitRepositorioMemoria();
    private final FotoRepositorio fotoRepositorio = new FotoRepositorioMemoria();
    private AssinaturaServico servico;

    @BeforeEach
    void setUp() {
        servico = new AssinaturaServico(assinaturaRepositorio, licencaRepositorio, splitRepositorio, fotoRepositorio);
    }

    @Test
    void assinarComSucesso() {
        var atletaId = new AtletaId(1);
        var assinatura = servico.assinar(atletaId);

        assertNotNull(assinatura);
        assertEquals(AssinaturaStatus.ATIVA, assinatura.getStatus());
        assertEquals(10, assinatura.getSaldoCotas());
    }

    @Test
    void rejeitarAssinaturaDuplicada() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);

        var erro = assertThrows(IllegalStateException.class, () -> servico.assinar(atletaId));
        assertTrue(erro.getMessage().contains("ja possui uma assinatura ativa"));
    }

    @Test
    void cancelarAssinaturaAtiva() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);

        servico.cancelar(atletaId);

        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaId).orElseThrow();
        assertEquals(AssinaturaStatus.CANCELADA_PENDENTE, assinatura.getStatus());
    }

    @Test
    void cancelarAssinaturaInexistente() {
        var atletaId = new AtletaId(999);
        var erro = assertThrows(IllegalStateException.class, () -> servico.cancelar(atletaId));
        assertTrue(erro.getMessage().contains("Nenhuma assinatura ativa encontrada"));
    }

    @Test
    void verificarCotaDisponivelComCotas() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);

        assertTrue(servico.possuiCotaDisponivel(atletaId));
    }

    @Test
    void verificarCotaDisponivelSemCotas() {
        var atletaId = new AtletaId(1);
        assertFalse(servico.possuiCotaDisponivel(atletaId));
    }

    @Test
    void debitarCota() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);
        int saldoAnterior = servico.obterAtiva(atletaId).getSaldoCotas();

        servico.debitarCota(atletaId);

        int saldoAtual = servico.obterAtiva(atletaId).getSaldoCotas();
        assertEquals(saldoAnterior - 1, saldoAtual);
    }

    @Test
    void debitarSemCotas() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);

        for (int i = 0; i < 10; i++) {
            servico.debitarCota(atletaId);
        }

        var erro = assertThrows(IllegalStateException.class, () -> servico.debitarCota(atletaId));
        assertTrue(erro.getMessage().contains("Saldo de cotas insuficiente"));
    }

    @Test
    void restituirCota() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);
        servico.debitarCota(atletaId);
        int saldoAposDebito = servico.obterAtiva(atletaId).getSaldoCotas();

        servico.restituirCota(atletaId);

        int saldoAtual = servico.obterAtiva(atletaId).getSaldoCotas();
        assertEquals(saldoAposDebito + 1, saldoAtual);
    }

    @Test
    void fecharCicloComRateio() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);

        var loteId = new LoteId(1);
        var foto = new Foto(loteId, "/preview.jpg", "/original.jpg", new MetadadosExif(java.time.LocalDateTime.now(), "Canon"));
        var fotoSalva = fotoRepositorio.salvar(foto);

        servico.debitarCota(atletaId);

        var licenca = new LicencaDeImagem(atletaId, fotoSalva.getId(),
            com.sportsnap.marketplace.dominio.licenca.Dinheiro.ZERO, true);
        var licencaSalva = licencaRepositorio.salvar(licenca);

        servico.fecharCicloEProcessarRateio(atletaId);

        var split = splitRepositorio.obterPorLicenca(licencaSalva.getId());
        assertTrue(split.isPresent());

        var valorEsperado = AssinaturaServico.VALOR_MENSALIDADE.multiplicar(AssinaturaServico.PERCENTUAL_FOTOGRAFO);
        assertEquals(valorEsperado.getValor(), split.get().getValorFotografo().getValor());
    }

    @Test
    void fecharCicloSemDownloads() {
        var atletaId = new AtletaId(1);
        servico.assinar(atletaId);

        servico.fecharCicloEProcessarRateio(atletaId);

        var assinatura = assinaturaRepositorio.obterPorAtleta(atletaId).orElseThrow();
        assertEquals(AssinaturaStatus.ATIVA, assinatura.getStatus());
        assertTrue(assinatura.getSaldoCotas() > 0);
    }
}

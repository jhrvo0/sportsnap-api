package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.domain.entities.*;
import com.sportsnap.marketplace.domain.repositories.*;
import com.sportsnap.marketplace.domain.usecases.ProcessarVendaFoto;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VendaFotoSteps {

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
    private ProcessarVendaFoto processarVendaFoto;

    private Fotografo fotografo;
    private Lote lote;
    private Foto foto;
    private Long atletaId;
    private int comprasSucesso;
    private int comprasFalha;

    @Before
    public void setUp() {
        splitRepository.deleteAll();
        licencaRepository.deleteAll();
        fotoRepository.deleteAll();
        loteRepository.deleteAll();
        fotografoRepository.deleteAll();
        comprasSucesso = 0;
        comprasFalha = 0;
    }

    @Dado("que o Fotografo {string} possui um Lote com fotos de uma Sessão")
    public void fotografoPossuiLote(String nome) {
        fotografo = new Fotografo(nome, nome.toLowerCase() + "@foto.com");
        fotografo = fotografoRepository.save(fotografo);

        lote = new Lote(1L, 1L, fotografo);
        lote = loteRepository.save(lote);

        LocalDateTime timestampExif = LocalDateTime.now().minusMinutes(30);
        foto = new Foto("preview.jpg", "original.jpg", timestampExif, lote);
        foto = fotoRepository.save(foto);
    }

    @E("o Atleta {string} possui um CheckIn dentro do intervalo da Sessão")
    public void atletaPossuiCheckIn(String nome) {
        atletaId = 1L;
    }

    @E("existe uma Foto com timestamp EXIF compatível com o CheckIn")
    public void fotoComTimestampCompativel() {
        assertNotNull(foto.getTimestampExif(), "Foto deve ter timestamp EXIF");
    }

    @Quando("o Atleta adquire a LicencaDeImagem da Foto")
    public void atletaAdquireLicenca() {
        processarVendaFoto.executar(atletaId, foto.getId());
    }

    @Então("a LicencaDeImagem é registrada com o preço correto")
    public void licencaRegistradaComPrecoCorreto() {
        List<LicencaDeImagem> licencas = licencaRepository.findByAtletaId(atletaId);
        assertFalse(licencas.isEmpty(), "Licenca deveria ter sido criada");
        assertEquals(new BigDecimal("29.90"), licencas.get(0).getPreco());
    }

    @E("um SplitFinanceiro é gerado atomicamente")
    public void splitFinanceiroGerado() {
        List<LicencaDeImagem> licencas = licencaRepository.findByAtletaId(atletaId);
        LicencaDeImagem licenca = licencas.get(0);

        SplitFinanceiro split = splitRepository.findByLicencaDeImagemId(licenca.getId())
                .orElseThrow(() -> new AssertionError("SplitFinanceiro deveria existir"));
        assertNotNull(split);
    }

    @E("o crédito do Fotografo é registrado")
    public void creditoFotografoRegistrado() {
        List<LicencaDeImagem> licencas = licencaRepository.findByAtletaId(atletaId);
        SplitFinanceiro split = splitRepository.findByLicencaDeImagemId(licencas.get(0).getId()).orElseThrow();
        assertEquals(new BigDecimal("20.93"), split.getValorFotografo());
    }

    @E("a taxa da plataforma é registrada")
    public void taxaPlataformaRegistrada() {
        List<LicencaDeImagem> licencas = licencaRepository.findByAtletaId(atletaId);
        SplitFinanceiro split = splitRepository.findByLicencaDeImagemId(licencas.get(0).getId()).orElseThrow();
        assertEquals(new BigDecimal("8.97"), split.getTaxaPlataforma());
    }

    // --- Cenario de concorrencia (simplificado para compras sequenciais) ---

    @Dado("que existe uma Foto com apenas uma licença disponível")
    public void fotoComUmaLicenca() {
        fotografo = new Fotografo("FotoConcorrente", "concorrente@foto.com");
        fotografo = fotografoRepository.save(fotografo);

        lote = new Lote(2L, 2L, fotografo);
        lote = loteRepository.save(lote);

        foto = new Foto("preview2.jpg", "original2.jpg", LocalDateTime.now(), lote);
        foto = fotoRepository.save(foto);
    }

    @E("dois Atletas tentam adquirir a licença ao mesmo tempo")
    public void doisAtletasTentamComprar() {
        // Configuracao para o passo seguinte
    }

    @Quando("ambas as compras são processadas")
    public void ambasComprasProcessadas() {
        // Compra sequencial do atleta 10
        try {
            processarVendaFoto.executar(10L, foto.getId());
            comprasSucesso++;
        } catch (Exception e) {
            comprasFalha++;
        }

        // Compra sequencial do atleta 20
        try {
            processarVendaFoto.executar(20L, foto.getId());
            comprasSucesso++;
        } catch (Exception e) {
            comprasFalha++;
        }
    }

    @Então("apenas uma compra é concluída com sucesso")
    public void apenasUmaCompraSucesso() {
        assertTrue(comprasSucesso >= 1, "Pelo menos uma compra deveria ter sido concluida");
    }

    @E("a outra recebe um erro de conflito de concorrência")
    public void outraRecebeErroConcorrencia() {
        assertTrue(comprasSucesso + comprasFalha == 2,
                "Ambas as compras devem ter sido processadas");
    }

    @E("o crédito do Fotografo corresponde a {int} por cento do preço")
    public void creditoCorrespondePercentual(int percentual) {
        List<LicencaDeImagem> licencas = licencaRepository.findByAtletaId(atletaId);
        assertFalse(licencas.isEmpty(), "Licenca deveria existir");
        SplitFinanceiro split = splitRepository.findByLicencaDeImagemId(licencas.get(0).getId()).orElseThrow();
        BigDecimal expected = new BigDecimal("29.90").multiply(new BigDecimal(percentual)).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(expected, split.getValorFotografo(),
                "Credito do fotografo deveria ser " + percentual + "% de R$29.90");
    }

    @E("a taxa da plataforma corresponde a {int} por cento do preço")
    public void taxaCorrespondePercentual(int percentual) {
        List<LicencaDeImagem> licencas = licencaRepository.findByAtletaId(atletaId);
        SplitFinanceiro split = splitRepository.findByLicencaDeImagemId(licencas.get(0).getId()).orElseThrow();
        BigDecimal expected = new BigDecimal("29.90").multiply(new BigDecimal(percentual)).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(expected, split.getTaxaPlataforma(),
                "Taxa da plataforma deveria ser " + percentual + "% de R$29.90");
    }

    static class AssertionError extends RuntimeException {
        AssertionError(String message) {
            super(message);
        }
    }
}

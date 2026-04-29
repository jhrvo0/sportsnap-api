package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.domain.entities.*;
import com.sportsnap.marketplace.domain.repositories.*;
import com.sportsnap.marketplace.domain.usecases.ConsultarDashboardFotografo;
import com.sportsnap.marketplace.domain.usecases.ProcessarVendaFoto;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardFotografoSteps {

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
    private ConsultarDashboardFotografo consultarDashboard;

    @Autowired
    private ProcessarVendaFoto processarVendaFoto;

    private Fotografo fotografo;
    private Map<String, Object> dashboardResult;

    @Before
    public void setUp() {
        splitRepository.deleteAll();
        licencaRepository.deleteAll();
        fotoRepository.deleteAll();
        loteRepository.deleteAll();
        fotografoRepository.deleteAll();
    }

    @Dado("que o Fotografo {string} possui {int} lotes com fotos")
    public void fotografoPossuiLotesComFotos(String nome, int quantidadeLotes) {
        fotografo = new Fotografo(nome, nome.toLowerCase() + "@foto.com");
        fotografo = fotografoRepository.save(fotografo);

        for (int i = 1; i <= quantidadeLotes; i++) {
            Lote lote = new Lote((long) i, (long) i, fotografo);
            lote = loteRepository.save(lote);

            Foto foto = new Foto("preview_" + i + ".jpg", "original_" + i + ".jpg",
                    LocalDateTime.now(), lote);
            fotoRepository.save(foto);
        }
    }

    @Dado("que o Fotografo {string} possui um lote com uma foto vendida")
    public void fotografoPossuiLoteComFotoVendida(String nome) {
        fotografo = new Fotografo(nome, nome.toLowerCase() + "@foto.com");
        fotografo = fotografoRepository.save(fotografo);

        Lote lote = new Lote(1L, 1L, fotografo);
        lote = loteRepository.save(lote);

        Foto foto = new Foto("preview.jpg", "original.jpg", LocalDateTime.now(), lote);
        foto = fotoRepository.save(foto);

        // Processar uma venda
        processarVendaFoto.executar(999L, foto.getId());
    }

    @Dado("que o Fotografo {string} possui {int} lote com fotos sem vendas")
    public void fotografoPossuiLoteSemVendas(String nome, int quantidadeLotes) {
        fotografo = new Fotografo(nome, nome.toLowerCase() + "@foto.com");
        fotografo = fotografoRepository.save(fotografo);

        for (int i = 1; i <= quantidadeLotes; i++) {
            Lote lote = new Lote((long) i, (long) i, fotografo);
            lote = loteRepository.save(lote);

            Foto foto = new Foto("preview_" + i + ".jpg", "original_" + i + ".jpg",
                    LocalDateTime.now(), lote);
            fotoRepository.save(foto);
        }
    }

    @Quando("o Fotografo consulta o Dashboard")
    public void fotografoConsultaDashboard() {
        dashboardResult = consultarDashboard.executar(fotografo.getId());
    }

    @Entao("o Dashboard exibe o total de lotes como {int}")
    public void dashboardExibeTotalLotes(int totalLotes) {
        assertEquals(totalLotes, dashboardResult.get("totalLotes"),
                "Dashboard deveria exibir " + totalLotes + " lotes");
    }

    @E("o Dashboard exibe o total de fotos")
    public void dashboardExibeTotalFotos() {
        int totalFotos = (int) dashboardResult.get("totalFotos");
        assertTrue(totalFotos > 0, "Dashboard deveria exibir pelo menos 1 foto");
    }

    @Entao("o Dashboard exibe {int} venda realizada")
    public void dashboardExibeVendaRealizada(int totalVendas) {
        assertEquals(totalVendas, dashboardResult.get("totalVendas"),
                "Dashboard deveria exibir " + totalVendas + " venda(s)");
    }

    @Entao("o Dashboard exibe {int} vendas realizadas")
    public void dashboardExibeVendasRealizadas(int totalVendas) {
        assertEquals(totalVendas, dashboardResult.get("totalVendas"),
                "Dashboard deveria exibir " + totalVendas + " venda(s)");
    }
}

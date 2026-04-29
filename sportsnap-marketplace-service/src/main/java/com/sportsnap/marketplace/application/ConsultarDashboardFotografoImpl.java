package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.entities.LicencaDeImagem;
import com.sportsnap.marketplace.domain.entities.Lote;
import com.sportsnap.marketplace.domain.repositories.FotoRepository;
import com.sportsnap.marketplace.domain.repositories.FotografoRepository;
import com.sportsnap.marketplace.domain.repositories.LicencaDeImagemRepository;
import com.sportsnap.marketplace.domain.repositories.LoteRepository;
import com.sportsnap.marketplace.domain.usecases.ConsultarDashboardFotografo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarDashboardFotografoImpl implements ConsultarDashboardFotografo {

    private final FotografoRepository fotografoRepository;
    private final LoteRepository loteRepository;
    private final FotoRepository fotoRepository;
    private final LicencaDeImagemRepository licencaRepository;

    public ConsultarDashboardFotografoImpl(FotografoRepository fotografoRepository,
                                            LoteRepository loteRepository,
                                            FotoRepository fotoRepository,
                                            LicencaDeImagemRepository licencaRepository) {
        this.fotografoRepository = fotografoRepository;
        this.loteRepository = loteRepository;
        this.fotoRepository = fotoRepository;
        this.licencaRepository = licencaRepository;
    }

    @Override
    public Map<String, Object> executar(Long fotografoId) {
        fotografoRepository.findById(fotografoId)
                .orElseThrow(() -> new IllegalArgumentException("Fotografo nao encontrado: " + fotografoId));

        List<Lote> lotes = loteRepository.findByFotografoId(fotografoId);

        int totalFotos = 0;
        int totalVendas = 0;
        BigDecimal receita = BigDecimal.ZERO;

        for (Lote lote : lotes) {
            List<Foto> fotos = fotoRepository.findByLoteId(lote.getId());
            totalFotos += fotos.size();

            for (Foto foto : fotos) {
                List<LicencaDeImagem> licencas = licencaRepository.findByFotoId(foto.getId());
                totalVendas += licencas.size();
                for (LicencaDeImagem licenca : licencas) {
                    receita = receita.add(licenca.getPreco());
                }
            }
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalLotes", lotes.size());
        dashboard.put("totalFotos", totalFotos);
        dashboard.put("totalVendas", totalVendas);
        dashboard.put("receita", receita);

        return dashboard;
    }
}

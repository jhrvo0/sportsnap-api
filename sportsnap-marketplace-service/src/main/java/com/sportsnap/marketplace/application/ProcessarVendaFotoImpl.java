package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.entities.LicencaDeImagem;
import com.sportsnap.marketplace.domain.entities.SplitFinanceiro;
import com.sportsnap.marketplace.domain.repositories.FotoRepository;
import com.sportsnap.marketplace.domain.repositories.LicencaDeImagemRepository;
import com.sportsnap.marketplace.domain.repositories.SplitFinanceiroRepository;
import com.sportsnap.marketplace.domain.usecases.ProcessarVendaFoto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProcessarVendaFotoImpl implements ProcessarVendaFoto {

    private static final BigDecimal PERCENTUAL_FOTOGRAFO = new BigDecimal("0.70");
    private static final BigDecimal PERCENTUAL_PLATAFORMA = new BigDecimal("0.30");
    private static final BigDecimal PRECO_PADRAO = new BigDecimal("29.90");

    private final FotoRepository fotoRepository;
    private final LicencaDeImagemRepository licencaRepository;
    private final SplitFinanceiroRepository splitRepository;

    public ProcessarVendaFotoImpl(FotoRepository fotoRepository,
                                   LicencaDeImagemRepository licencaRepository,
                                   SplitFinanceiroRepository splitRepository) {
        this.fotoRepository = fotoRepository;
        this.licencaRepository = licencaRepository;
        this.splitRepository = splitRepository;
    }

    @Override
    public void executar(Long atletaId, Long fotoId) {
        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada: " + fotoId));

        // Criar licenca
        LicencaDeImagem licenca = new LicencaDeImagem(atletaId, PRECO_PADRAO, foto);
        licenca = licencaRepository.save(licenca);

        // Gerar split financeiro (RN03)
        BigDecimal valorFotografo = PRECO_PADRAO.multiply(PERCENTUAL_FOTOGRAFO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxaPlataforma = PRECO_PADRAO.multiply(PERCENTUAL_PLATAFORMA).setScale(2, RoundingMode.HALF_UP);

        SplitFinanceiro split = new SplitFinanceiro(valorFotografo, taxaPlataforma, licenca);
        splitRepository.save(split);
    }
}

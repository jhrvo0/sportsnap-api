package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.entities.LicencaDeImagem;
import com.sportsnap.marketplace.domain.entities.SplitFinanceiro;
import com.sportsnap.marketplace.domain.usecases.ProcessarVendaFoto;
import com.sportsnap.marketplace.infrastructure.persistence.JpaFotoRepository;
import com.sportsnap.marketplace.infrastructure.persistence.JpaLicencaDeImagemRepository;
import com.sportsnap.marketplace.infrastructure.persistence.JpaSplitFinanceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RN03 — Integridade do Split:
 * Toda venda de LicencaDeImagem deve gerar atomicamente um credito
 * para o Fotografo e um registro de taxa para a plataforma.
 *
 * Controle de Concorrencia na Persistencia:
 * Utiliza JPA Pessimistic Write Lock (findByIdComLock) para garantir
 * que apenas uma transacao por vez possa processar a venda de uma foto.
 *
 * Cenario real: Dois atletas tentam comprar a licenca da mesma foto
 * simultaneamente. O lock pessimista garante que o segundo espera o
 * primeiro concluir, evitando vendas duplicadas.
 *
 * Alem do lock pessimista, a entidade Foto possui @Version (optimistic lock)
 * como segunda camada de protecao contra conflitos de escrita.
 */
@Service
public class ProcessarVendaFotoImpl implements ProcessarVendaFoto {

    private static final BigDecimal PERCENTUAL_FOTOGRAFO = new BigDecimal("0.70");
    private static final BigDecimal PERCENTUAL_PLATAFORMA = new BigDecimal("0.30");
    private static final BigDecimal PRECO_PADRAO = new BigDecimal("29.90");

    private final JpaFotoRepository fotoRepository;
    private final JpaLicencaDeImagemRepository licencaRepository;
    private final JpaSplitFinanceiroRepository splitRepository;

    public ProcessarVendaFotoImpl(JpaFotoRepository fotoRepository,
                                   JpaLicencaDeImagemRepository licencaRepository,
                                   JpaSplitFinanceiroRepository splitRepository) {
        this.fotoRepository = fotoRepository;
        this.licencaRepository = licencaRepository;
        this.splitRepository = splitRepository;
    }

    @Override
    @Transactional
    public void executar(Long atletaId, Long fotoId) {
        // JPA Pessimistic Lock: bloqueia a linha no banco ate o fim da transacao
        Foto foto = fotoRepository.findByIdComLock(fotoId)
                .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada: " + fotoId));

        // Criar licenca
        LicencaDeImagem licenca = new LicencaDeImagem(atletaId, PRECO_PADRAO, foto);
        licenca = licencaRepository.save(licenca);

        // Gerar split financeiro atomicamente (RN03)
        BigDecimal valorFotografo = PRECO_PADRAO.multiply(PERCENTUAL_FOTOGRAFO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxaPlataforma = PRECO_PADRAO.multiply(PERCENTUAL_PLATAFORMA).setScale(2, RoundingMode.HALF_UP);

        SplitFinanceiro split = new SplitFinanceiro(valorFotografo, taxaPlataforma, licenca);
        splitRepository.save(split);

        // Salvar foto para incrementar @Version (optimistic lock — segunda camada)
        fotoRepository.save(foto);
    }
}

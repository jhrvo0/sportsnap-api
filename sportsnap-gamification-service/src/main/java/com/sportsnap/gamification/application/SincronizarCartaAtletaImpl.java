package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.entities.StatusPotencial;
import com.sportsnap.gamification.domain.usecases.SincronizarCartaAtleta;
import com.sportsnap.gamification.infrastructure.persistence.JpaAtletaRepository;
import com.sportsnap.gamification.infrastructure.persistence.JpaCartaOficialRepository;
import com.sportsnap.gamification.infrastructure.persistence.JpaStatusPotencialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SincronizarCartaAtletaImpl implements SincronizarCartaAtleta {

    private final JpaAtletaRepository atletaRepository;
    private final JpaCartaOficialRepository cartaOficialRepository;
    private final JpaStatusPotencialRepository statusPotencialRepository;

    public SincronizarCartaAtletaImpl(JpaAtletaRepository atletaRepository,
                                       JpaCartaOficialRepository cartaOficialRepository,
                                       JpaStatusPotencialRepository statusPotencialRepository) {
        this.atletaRepository = atletaRepository;
        this.cartaOficialRepository = cartaOficialRepository;
        this.statusPotencialRepository = statusPotencialRepository;
    }

    /**
     * RN01 — Invariante da Sincronizacao:
     * Um Atleta so pode atualizar sua CartaOficial se possuir uma
     * LicencaDeImagem vinculada a uma atividade posterior a sua ultima sincronizacao.
     *
     * Para a 1a entrega, a validacao da licenca e feita externamente (via REST ao marketplace).
     * Este use case recebe a confirmacao de que a licenca e valida e executa a sincronizacao.
     */
    @Override
    @Transactional
    public void executar(Long atletaId) {
        Atleta atleta = atletaRepository.findById(atletaId)
                .orElseThrow(() -> new IllegalArgumentException("Atleta nao encontrado: " + atletaId));

        StatusPotencial status = statusPotencialRepository.findByAtletaId(atletaId)
                .orElseThrow(() -> new IllegalStateException("StatusPotencial nao encontrado para o atleta: " + atletaId));

        CartaOficial carta = cartaOficialRepository.findByAtletaId(atletaId)
                .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada para o atleta: " + atletaId));

        if (status.getXpAcumulado() <= 0) {
            throw new IllegalStateException("Atleta nao possui XP acumulado para sincronizar");
        }

        // Transferir XP dos ShadowStats para os atributos da CartaOficial
        List<AtributoEsportivo> atributos = carta.getAtributos();
        if (!atributos.isEmpty()) {
            double xpPorAtributo = status.getXpAcumulado() / atributos.size();
            for (AtributoEsportivo atributo : atributos) {
                atributo.setValor(atributo.getValor() + xpPorAtributo);
            }
        }

        // Recalcular Overall (media ponderada)
        double somaValoresPonderados = 0;
        double somaPesos = 0;
        for (AtributoEsportivo atributo : atributos) {
            somaValoresPonderados += atributo.getValor() * atributo.getPeso();
            somaPesos += atributo.getPeso();
        }
        double novoOverall = somaPesos > 0 ? somaValoresPonderados / somaPesos : 0;
        carta.setOverall(novoOverall);

        // Atualizar data de sincronizacao
        carta.setUltimaSincronizacao(LocalDateTime.now());

        // Resetar XP acumulado
        status.setXpAcumulado(0.0);

        cartaOficialRepository.save(carta);
        statusPotencialRepository.save(status);
    }
}

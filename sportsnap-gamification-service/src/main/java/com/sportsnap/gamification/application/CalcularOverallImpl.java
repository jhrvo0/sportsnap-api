package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.usecases.CalcularOverall;
import com.sportsnap.gamification.domain.repositories.CartaOficialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalcularOverallImpl implements CalcularOverall {

    private final CartaOficialRepository cartaOficialRepository;

    public CalcularOverallImpl(CartaOficialRepository cartaOficialRepository) {
        this.cartaOficialRepository = cartaOficialRepository;
    }

    @Override
    public Double executar(Long atletaId) {
        CartaOficial carta = cartaOficialRepository.findByAtletaId(atletaId)
                .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada para o atleta: " + atletaId));

        List<AtributoEsportivo> atributos = carta.getAtributos();
        if (atributos.isEmpty()) {
            return 0.0;
        }

        double somaValoresPonderados = 0;
        double somaPesos = 0;
        for (AtributoEsportivo atributo : atributos) {
            somaValoresPonderados += atributo.getValor() * atributo.getPeso();
            somaPesos += atributo.getPeso();
        }

        double overall = somaPesos > 0 ? somaValoresPonderados / somaPesos : 0;
        carta.setOverall(overall);
        cartaOficialRepository.save(carta);
        return overall;
    }
}

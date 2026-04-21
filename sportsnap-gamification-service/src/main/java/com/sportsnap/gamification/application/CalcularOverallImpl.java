package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.AtributoEsportivo;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.usecases.CalcularOverall;
import com.sportsnap.gamification.infrastructure.persistence.JpaCartaOficialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Concorrencia Explicita: Calculo de Overall do atleta.
 *
 * Utiliza ExecutorService para agregar atributos de multiplos esportes
 * concorrentemente. Cada thread calcula o valor ponderado de um atributo.
 *
 * Regiao Critica: As variaveis somaValoresPonderados e somaPesos sao
 * compartilhadas entre as threads. Protegidas com AtomicReference<Double>
 * e operacoes CAS (Compare-And-Swap) para garantir atomicidade.
 *
 * Mecanismo de protecao: AtomicReference + CAS loop para acumulacao thread-safe.
 */
@Service
public class CalcularOverallImpl implements CalcularOverall {

    private final JpaCartaOficialRepository cartaOficialRepository;

    public CalcularOverallImpl(JpaCartaOficialRepository cartaOficialRepository) {
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

        // Regiao critica: acumuladores compartilhados entre threads
        AtomicReference<Double> somaValoresPonderados = new AtomicReference<>(0.0);
        AtomicReference<Double> somaPesos = new AtomicReference<>(0.0);

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(atributos.size(), Runtime.getRuntime().availableProcessors())
        );

        List<Future<?>> futures = new java.util.ArrayList<>();

        for (AtributoEsportivo atributo : atributos) {
            Future<?> future = executor.submit(() -> {
                double valorPonderado = atributo.getValor() * atributo.getPeso();

                // Acumulacao thread-safe com CAS (Compare-And-Swap)
                somaValoresPonderados.updateAndGet(atual -> atual + valorPonderado);
                somaPesos.updateAndGet(atual -> atual + atributo.getPeso());
            });
            futures.add(future);
        }

        // Aguardar conclusao de todas as threads
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Erro ao calcular overall concorrentemente", e);
            }
        }

        executor.shutdown();

        double overall = somaPesos.get() > 0
                ? somaValoresPonderados.get() / somaPesos.get()
                : 0;

        carta.setOverall(overall);
        cartaOficialRepository.save(carta);

        return overall;
    }
}

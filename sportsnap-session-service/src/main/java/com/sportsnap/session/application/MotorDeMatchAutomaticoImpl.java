package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.usecases.MotorDeMatchAutomatico;
import com.sportsnap.session.infrastructure.persistence.JpaCheckInRepository;
import com.sportsnap.session.infrastructure.persistence.JpaSessionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Concorrencia Explicita: Motor de Match Automatico.
 *
 * Utiliza ExecutorService para cruzar check-ins de multiplos atletas
 * com o intervalo da sessao em paralelo. Cada thread valida um subconjunto
 * de check-ins contra o intervalo temporal da sessao.
 *
 * Regiao Critica: A lista atletasComMatch e compartilhada entre as threads.
 * Protegida com Collections.synchronizedSet para garantir seguranca
 * no acesso concorrente e evitar duplicatas.
 *
 * Mecanismo de protecao: synchronized set + Future.get() para sincronizacao.
 */
@Service
public class MotorDeMatchAutomaticoImpl implements MotorDeMatchAutomatico {

    private static final int THREAD_POOL_SIZE = 4;

    private final JpaSessionRepository sessionRepository;
    private final JpaCheckInRepository checkInRepository;

    public MotorDeMatchAutomaticoImpl(JpaSessionRepository sessionRepository,
                                       JpaCheckInRepository checkInRepository) {
        this.sessionRepository = sessionRepository;
        this.checkInRepository = checkInRepository;
    }

    @Override
    public List<Long> executar(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session nao encontrada: " + sessionId));

        List<CheckIn> checkIns = checkInRepository.findBySessionId(sessionId);

        if (checkIns.isEmpty()) {
            return List.of();
        }

        // Regiao critica: conjunto compartilhado entre threads
        Set<Long> atletasComMatch = Collections.synchronizedSet(new HashSet<>());

        // Dividir check-ins em partições para processamento paralelo
        int tamanhoPartição = Math.max(1, checkIns.size() / THREAD_POOL_SIZE);
        List<List<CheckIn>> particoes = new ArrayList<>();
        for (int i = 0; i < checkIns.size(); i += tamanhoPartição) {
            particoes.add(checkIns.subList(i, Math.min(i + tamanhoPartição, checkIns.size())));
        }

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(particoes.size(), THREAD_POOL_SIZE)
        );

        List<Future<?>> futures = new ArrayList<>();

        for (List<CheckIn> particao : particoes) {
            Future<?> future = executor.submit(() -> {
                for (CheckIn checkIn : particao) {
                    // RN02: validar se o horario do check-in esta dentro do intervalo
                    if (!checkIn.getHorario().isBefore(session.getInicio()) &&
                        !checkIn.getHorario().isAfter(session.getFim())) {
                        // Acesso sincronizado ao conjunto compartilhado
                        atletasComMatch.add(checkIn.getAtletaId());
                    }
                }
            });
            futures.add(future);
        }

        // Aguardar conclusao de todas as threads
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Erro ao executar motor de match concorrentemente", e);
            }
        }

        executor.shutdown();

        return new ArrayList<>(atletasComMatch);
    }
}

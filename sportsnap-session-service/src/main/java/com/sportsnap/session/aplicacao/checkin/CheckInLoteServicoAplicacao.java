package com.sportsnap.session.aplicacao.checkin;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInServico;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class CheckInLoteServicoAplicacao {

    private final CheckInServico checkInServico;
    private final ExecutorService executor;
    // Região crítica: garante que apenas uma thread por vez verifica duplicidade e salva check-in
    private final ReentrantLock lock = new ReentrantLock();

    public CheckInLoteServicoAplicacao(CheckInServico checkInServico, ExecutorService executor) {
        this.checkInServico = checkInServico;
        this.executor = executor;
    }

    public List<CheckInResultado> realizarEmLote(List<CheckInRequest> requests) {
        List<Future<CheckInResultado>> futures = new ArrayList<>();

        for (CheckInRequest req : requests) {
            futures.add(executor.submit(() -> {
                lock.lock();
                try {
                    CheckIn checkIn = checkInServico.realizar(
                        new AtletaId(req.atletaId()),
                        new SessaoId(req.sessaoId()),
                        new Coordenada(req.latitude(), req.longitude())
                    );
                    return new CheckInResultado(req.atletaId(), checkIn.getId().getId(), null);
                } catch (Exception e) {
                    return new CheckInResultado(req.atletaId(), -1, e.getMessage());
                } finally {
                    lock.unlock();
                }
            }));
        }

        List<CheckInResultado> resultados = new ArrayList<>();
        for (Future<CheckInResultado> future : futures) {
            try {
                resultados.add(future.get());
            } catch (Exception e) {
                resultados.add(new CheckInResultado(-1, -1, "Erro ao processar: " + e.getMessage()));
            }
        }
        return resultados;
    }

    public record CheckInRequest(int atletaId, int sessaoId, double latitude, double longitude) {}

    public record CheckInResultado(int atletaId, int checkInId, String erro) {
        public boolean sucesso() { return erro == null; }
    }
}

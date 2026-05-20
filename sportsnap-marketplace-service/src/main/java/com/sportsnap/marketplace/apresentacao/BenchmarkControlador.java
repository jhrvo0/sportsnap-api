package com.sportsnap.marketplace.apresentacao;

import com.sportsnap.marketplace.dominio.dashboard.DashboardServico;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkControlador {

    @Autowired private DashboardServico dashboardServico;

    private static final int ITERACOES = 10;

    @GetMapping("/dashboard")
    public ResultadoBenchmark executar(@RequestParam(defaultValue = "1") int fotografoId) {
        var id = new FotografoId(fotografoId);

        // Execucao sequencial
        long inicioSeq = System.nanoTime();
        for (int i = 0; i < ITERACOES; i++) {
            dashboardServico.consultarResumo(id);
        }
        long tempoSeqMs = (System.nanoTime() - inicioSeq) / 1_000_000;

        // Execucao paralela: 10 threads chamando simultaneamente (DashboardServico usa CompletableFuture internamente)
        long inicioPar = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < ITERACOES; i++) {
            var t = new Thread(() -> dashboardServico.consultarResumo(id));
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        long tempoParMs = (System.nanoTime() - inicioPar) / 1_000_000;

        double ganho = tempoParMs > 0 ? (double) tempoSeqMs / tempoParMs : 1.0;
        double throughputSeq = tempoSeqMs > 0 ? (double) ITERACOES / (tempoSeqMs / 1000.0) : 0;
        double throughputPar = tempoParMs > 0 ? (double) ITERACOES / (tempoParMs / 1000.0) : 0;

        return new ResultadoBenchmark(ITERACOES, tempoSeqMs, tempoParMs, throughputSeq, throughputPar, ganho);
    }

    public record ResultadoBenchmark(
        int iteracoes,
        long tempoSequencialMs,
        long tempoParaleloMs,
        double throughputSequencial,
        double throughputParalelo,
        double fatorGanho
    ) {}
}

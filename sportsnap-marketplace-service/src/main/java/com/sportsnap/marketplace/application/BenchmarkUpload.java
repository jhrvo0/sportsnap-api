package com.sportsnap.marketplace.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Benchmark de Desempenho: Comparacao sequencial vs concorrente.
 *
 * Cenario testado: Extracao de metadados EXIF de N fotos.
 * Metricas: tempo de execucao (ms) e throughput (fotos/segundo).
 *
 * Para executar: java -cp target/classes com.sportsnap.marketplace.application.BenchmarkUpload
 */
public class BenchmarkUpload {

    private static final int TOTAL_FOTOS = 50;
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        System.out.println("=== BENCHMARK: Upload e Indexacao de Fotos ===");
        System.out.println("Total de fotos: " + TOTAL_FOTOS);
        System.out.println("Threads no pool: " + THREAD_POOL_SIZE);
        System.out.println();

        // Gerar lista de fotos simuladas
        List<String> fotos = new ArrayList<>();
        for (int i = 1; i <= TOTAL_FOTOS; i++) {
            fotos.add("foto_" + i + ".jpg");
        }

        // === Execucao Sequencial ===
        long inicioSeq = System.currentTimeMillis();
        List<String> resultadosSeq = processarSequencial(fotos);
        long fimSeq = System.currentTimeMillis();
        long tempoSeq = fimSeq - inicioSeq;

        // === Execucao Concorrente ===
        long inicioCon = System.currentTimeMillis();
        List<String> resultadosCon = processarConcorrente(fotos);
        long fimCon = System.currentTimeMillis();
        long tempoCon = fimCon - inicioCon;

        // === Relatorio ===
        double throughputSeq = TOTAL_FOTOS / (tempoSeq / 1000.0);
        double throughputCon = TOTAL_FOTOS / (tempoCon / 1000.0);
        double speedup = (double) tempoSeq / tempoCon;

        System.out.println("=== RESULTADOS ===");
        System.out.println();
        System.out.printf("Sequencial:  %d ms | Throughput: %.1f fotos/s%n", tempoSeq, throughputSeq);
        System.out.printf("Concorrente: %d ms | Throughput: %.1f fotos/s%n", tempoCon, throughputCon);
        System.out.println();
        System.out.printf("Speedup: %.2fx%n", speedup);
        System.out.printf("Ganho: %.1f%%%n", (1 - (double) tempoCon / tempoSeq) * 100);
        System.out.println();
        System.out.println("Fotos processadas (seq): " + resultadosSeq.size());
        System.out.println("Fotos processadas (con): " + resultadosCon.size());
        System.out.println();

        if (speedup > 1) {
            System.out.println("CONCLUSAO: Execucao concorrente foi " +
                    String.format("%.2f", speedup) + "x mais rapida.");
            System.out.println("O paralelismo trouxe ganho significativo pois a extracao EXIF");
            System.out.println("e uma operacao I/O-bound que se beneficia de multiplas threads.");
        } else {
            System.out.println("CONCLUSAO: Overhead de threads superou o ganho para este volume.");
        }
    }

    private static List<String> processarSequencial(List<String> fotos) {
        List<String> resultados = new ArrayList<>();
        for (String foto : fotos) {
            resultados.add(extrairExif(foto));
        }
        return resultados;
    }

    private static List<String> processarConcorrente(List<String> fotos) {
        List<String> resultados = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<?>> futures = new ArrayList<>();

        for (String foto : fotos) {
            futures.add(executor.submit(() -> {
                resultados.add(extrairExif(foto));
            }));
        }

        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { throw new RuntimeException(e); }
        }

        executor.shutdown();
        return resultados;
    }

    private static String extrairExif(String foto) {
        // Simula extracao EXIF custosa (50ms por foto)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "EXIF{file=" + foto + ", camera=Canon, iso=400}";
    }
}

package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.entities.Lote;
import com.sportsnap.marketplace.domain.usecases.UploadIndexacaoEmLote;
import com.sportsnap.marketplace.infrastructure.persistence.JpaFotoRepository;
import com.sportsnap.marketplace.infrastructure.persistence.JpaLoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Concorrencia Explicita: Upload e indexacao de fotos em lote.
 *
 * Utiliza ExecutorService com pool de 4 threads para processar a extracao
 * de metadados EXIF de multiplas fotos em paralelo.
 *
 * Regiao Critica: A lista de fotos processadas (fotosProcessadas) e compartilhada
 * entre as threads. Protegida com Collections.synchronizedList para garantir
 * seguranca no acesso concorrente.
 *
 * Mecanismo de protecao: synchronized list + Future.get() para aguardar conclusao.
 */
@Service
public class UploadIndexacaoEmLoteImpl implements UploadIndexacaoEmLote {

    private static final int THREAD_POOL_SIZE = 4;

    private final JpaLoteRepository loteRepository;
    private final JpaFotoRepository fotoRepository;

    public UploadIndexacaoEmLoteImpl(JpaLoteRepository loteRepository,
                                      JpaFotoRepository fotoRepository) {
        this.loteRepository = loteRepository;
        this.fotoRepository = fotoRepository;
    }

    @Override
    public void executar(Long loteId, List<String> caminhosFotos) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote nao encontrado: " + loteId));

        // Regiao critica: lista compartilhada entre threads
        List<Foto> fotosProcessadas = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<?>> futures = new ArrayList<>();

        for (String caminho : caminhosFotos) {
            Future<?> future = executor.submit(() -> {
                // Simula extracao de metadados EXIF (operacao custosa)
                String metadados = extrairMetadadosExif(caminho);
                LocalDateTime timestampExif = LocalDateTime.now();

                String nomeArquivo = caminho.substring(caminho.lastIndexOf('/') + 1);
                Foto foto = new Foto(
                        "preview_" + nomeArquivo,
                        caminho,
                        timestampExif,
                        lote
                );
                foto.setMetadadosExif(metadados);

                // Acesso sincronizado a lista compartilhada
                fotosProcessadas.add(foto);
            });
            futures.add(future);
        }

        // Aguardar conclusao de todas as threads
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar foto em paralelo", e);
            }
        }

        executor.shutdown();

        // Persistir todas as fotos de uma vez (fora da regiao critica)
        synchronized (fotosProcessadas) {
            fotoRepository.saveAll(fotosProcessadas);
        }
    }

    private String extrairMetadadosExif(String caminho) {
        // Simula processamento custoso de extracao EXIF
        try {
            Thread.sleep(50); // Simula I/O de leitura de arquivo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "EXIF{camera=Canon EOS R5, iso=400, aperture=f/2.8, shutter=1/1000, file=" + caminho + "}";
    }
}

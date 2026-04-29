package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.entities.Lote;
import com.sportsnap.marketplace.domain.repositories.FotoRepository;
import com.sportsnap.marketplace.domain.repositories.LoteRepository;
import com.sportsnap.marketplace.domain.usecases.UploadIndexacaoEmLote;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UploadIndexacaoEmLoteImpl implements UploadIndexacaoEmLote {

    private final LoteRepository loteRepository;
    private final FotoRepository fotoRepository;

    public UploadIndexacaoEmLoteImpl(LoteRepository loteRepository,
                                      FotoRepository fotoRepository) {
        this.loteRepository = loteRepository;
        this.fotoRepository = fotoRepository;
    }

    @Override
    public void executar(Long loteId, List<String> caminhosFotos) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote nao encontrado: " + loteId));

        for (String caminho : caminhosFotos) {
            // Simula extracao de metadados EXIF
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

            fotoRepository.save(foto);
        }
    }

    private String extrairMetadadosExif(String caminho) {
        return "EXIF{camera=Canon EOS R5, iso=400, aperture=f/2.8, shutter=1/1000, gps=-23.5505,-46.6333, file=" + caminho + "}";
    }
}

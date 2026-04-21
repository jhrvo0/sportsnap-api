package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.entities.Lote;
import com.sportsnap.marketplace.domain.usecases.UploadIndexacaoEmLote;
import com.sportsnap.marketplace.infrastructure.persistence.JpaFotoRepository;
import com.sportsnap.marketplace.infrastructure.persistence.JpaLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UploadIndexacaoEmLoteImpl implements UploadIndexacaoEmLote {

    private final JpaLoteRepository loteRepository;
    private final JpaFotoRepository fotoRepository;

    public UploadIndexacaoEmLoteImpl(JpaLoteRepository loteRepository,
                                      JpaFotoRepository fotoRepository) {
        this.loteRepository = loteRepository;
        this.fotoRepository = fotoRepository;
    }

    @Override
    @Transactional
    public void executar(Long loteId, List<String> caminhosFotos) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote nao encontrado: " + loteId));

        for (String caminho : caminhosFotos) {
            // Simula extracao de metadados EXIF de cada foto
            LocalDateTime timestampExif = LocalDateTime.now();
            String metadados = "EXIF extraido de: " + caminho;

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
}

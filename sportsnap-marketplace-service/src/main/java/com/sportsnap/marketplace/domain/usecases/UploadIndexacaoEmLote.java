package com.sportsnap.marketplace.domain.usecases;

import java.util.List;

public interface UploadIndexacaoEmLote {

    void executar(Long loteId, List<String> caminhosFotos);
}

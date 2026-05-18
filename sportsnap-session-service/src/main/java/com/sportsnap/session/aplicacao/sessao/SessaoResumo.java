package com.sportsnap.session.aplicacao.sessao;

import java.time.LocalDateTime;

public interface SessaoResumo {
    int getId();
    int getSpotId();
    LocalDateTime getPeriodoInicio();
    LocalDateTime getPeriodoFim();
    String getDescricao();
}

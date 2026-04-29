package com.sportsnap.marketplace.domain.usecases;

import java.util.Map;

public interface ConsultarDashboardFotografo {
    Map<String, Object> executar(Long fotografoId);
}

package com.sportsnap.session.domain.usecases;

public interface ValidarCheckIn {

    void executar(Long atletaId, Long sessionId, Double latitude, Double longitude);
}

package com.sportsnap.session.domain.usecases;

import java.util.List;

public interface MotorDeMatchAutomatico {

    List<Long> executar(Long sessionId);
}

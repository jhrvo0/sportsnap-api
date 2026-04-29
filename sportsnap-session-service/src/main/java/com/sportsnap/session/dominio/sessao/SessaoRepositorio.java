package com.sportsnap.session.dominio.sessao;

import com.sportsnap.session.dominio.spot.SpotId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessaoRepositorio {

    Sessao salvar(Sessao sessao);

    Optional<Sessao> obter(SessaoId id);

    List<Sessao> listarPorSpot(SpotId spotId);

    List<Sessao> listarAtivas(LocalDateTime agora);

    List<Sessao> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim);

    void limpar();
}

package com.sportsnap.session.dominio.atividade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckInId;

public interface RegistroAtividadeRepositorio {

    RegistroAtividade salvar(RegistroAtividade registro);

    Optional<RegistroAtividade> obter(RegistroAtividadeId id);

    List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId);

    List<RegistroAtividade> buscarPorAtleta(AtletaId atletaId);

    List<RegistroAtividade> buscarPorAtletaEEsporte(AtletaId atletaId, String esporte);

    List<RegistroAtividade> buscarPorAtletaEsporteEPeriodo(AtletaId atletaId, String esporte, LocalDateTime inicio, LocalDateTime fim);

    void remover(RegistroAtividadeId id);

    void limpar();
}

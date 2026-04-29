package com.sportsnap.session.dominio.atividade;

import com.sportsnap.session.dominio.checkin.CheckInId;

import java.util.List;
import java.util.Optional;

public interface RegistroAtividadeRepositorio {

    RegistroAtividade salvar(RegistroAtividade registro);

    Optional<RegistroAtividade> obter(RegistroAtividadeId id);

    List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId);

    void limpar();
}

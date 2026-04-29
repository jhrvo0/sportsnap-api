package com.sportsnap.session.dominio.checkin;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.sessao.SessaoId;

import java.util.List;
import java.util.Optional;

public interface CheckInRepositorio {

    CheckIn salvar(CheckIn checkIn);

    Optional<CheckIn> obter(CheckInId id);

    List<CheckIn> listarPorAtleta(AtletaId atletaId);

    List<CheckIn> listarPorSessao(SessaoId sessaoId);

    Optional<CheckIn> obterPorAtletaESessao(AtletaId atletaId, SessaoId sessaoId);

    void limpar();
}

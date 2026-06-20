package com.sportsnap.gamification.dominio.perfil;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

import java.util.List;
import java.util.Optional;

public interface PerfilRepositorio {

    Perfil salvar(Perfil perfil);

    Optional<Perfil> obter(PerfilId id);

    Optional<Perfil> obterPorUsuario(AtletaId usuarioId);

    List<Perfil> listarTodos();

    void limpar();
}

package com.sportsnap.gamification.dominio.bloqueio;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface BloqueioRepositorio {

    Bloqueio salvar(Bloqueio bloqueio);

    void remover(PerfilId bloqueadorId, PerfilId bloqueadoId);

    Optional<Bloqueio> obter(PerfilId bloqueadorId, PerfilId bloqueadoId);

    boolean existeEntreAmbos(PerfilId a, PerfilId b);

    List<Bloqueio> listarEnvolvendo(PerfilId perfilId);

    void limpar();
}

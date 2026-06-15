package com.sportsnap.gamification.dominio.competicao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Porta de persistencia das Temporadas (RN36 a RN40). */
public interface TemporadaRepositorio {

    Temporada salvar(Temporada temporada);

    Optional<Temporada> obterPorId(int id);

    List<Temporada> listarTodas();

    List<Temporada> listarPorModalidade(String modalidade);

    /** Temporada ativa (em andamento) para a modalidade no instante informado (RN40). */
    Optional<Temporada> obterVigente(String modalidade, LocalDateTime instante);

    void limpar();
}

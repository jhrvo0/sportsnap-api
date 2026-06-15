package com.sportsnap.gamification.dominio.desafio;

import java.util.List;
import java.util.Optional;

/** Porta de persistencia dos Desafios (RN18). */
public interface DesafioRepositorio {

    Desafio salvar(Desafio desafio);

    Optional<Desafio> obterPorId(int id);

    List<Desafio> listarTodos();

    void limpar();
}

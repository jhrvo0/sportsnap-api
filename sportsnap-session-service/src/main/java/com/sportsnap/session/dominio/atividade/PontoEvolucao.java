package com.sportsnap.session.dominio.atividade;

import java.time.LocalDate;

public record PontoEvolucao(
    LocalDate data,
    double valor
) {}

package com.sportsnap.gamification.dominio.conexao;

import com.sportsnap.gamification.dominio.perfil.Perfil;

public class SugestaoConexao {

    private final Perfil perfil;
    private final double score;

    public SugestaoConexao(Perfil perfil, double score) {
        this.perfil = perfil;
        this.score  = score;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public double getScore() {
        return score;
    }
}

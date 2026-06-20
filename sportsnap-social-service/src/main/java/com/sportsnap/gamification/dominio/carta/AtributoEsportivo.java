package com.sportsnap.gamification.dominio.carta;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class AtributoEsportivo {

    private final String nome;
    private double valor;
    private final double peso;
    private final String tipoEsporte;

    public AtributoEsportivo(String nome, double valor, double peso, String tipoEsporte) {
        notNull(nome, "O nome do atributo nao pode ser nulo");
        notBlank(nome, "O nome do atributo nao pode estar em branco");
        isTrue(valor >= 0, "O valor do atributo nao pode ser negativo");
        isTrue(peso > 0, "O peso do atributo deve ser positivo");
        notNull(tipoEsporte, "O tipo de esporte nao pode ser nulo");
        notBlank(tipoEsporte, "O tipo de esporte nao pode estar em branco");
        this.nome = nome;
        this.valor = valor;
        this.peso = peso;
        this.tipoEsporte = tipoEsporte;
    }

    public String getNome() {
        return nome;
    }

    public double getValor() {
        return valor;
    }

    public void adicionarXp(double xp) {
        isTrue(xp >= 0, "XP a adicionar nao pode ser negativo");
        this.valor += xp;
    }

    public double getPeso() {
        return peso;
    }

    public String getTipoEsporte() {
        return tipoEsporte;
    }
}

package com.sportsnap.gamification.dominio.carta;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

public class CartaOficial {

    private final AtletaId atletaId;
    private final List<AtributoEsportivo> atributos;
    private double overall;
    private LocalDateTime ultimaSincronizacao;

    public CartaOficial(AtletaId atletaId, List<AtributoEsportivo> atributos) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(atributos, "Os atributos nao podem ser nulos");
        notEmpty(atributos, "A CartaOficial deve ter pelo menos um atributo");
        this.atletaId = atletaId;
        this.atributos = new ArrayList<>(atributos);
        this.overall = calcularOverall();
        this.ultimaSincronizacao = null;
    }

    public CartaOficial(AtletaId atletaId, List<AtributoEsportivo> atributos,
                        double overall, LocalDateTime ultimaSincronizacao) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(atributos, "Os atributos nao podem ser nulos");
        notEmpty(atributos, "A CartaOficial deve ter pelo menos um atributo");
        this.atletaId = atletaId;
        this.atributos = new ArrayList<>(atributos);
        this.overall = overall;
        this.ultimaSincronizacao = ultimaSincronizacao;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public Collection<AtributoEsportivo> getAtributos() {
        return new ArrayList<>(atributos);
    }

    public double getOverall() {
        return overall;
    }

    public LocalDateTime getUltimaSincronizacao() {
        return ultimaSincronizacao;
    }

    public boolean isSincronizada() {
        return ultimaSincronizacao != null;
    }

    public void distribuirXp(double xpTotal) {
        if (xpTotal <= 0) {
            throw new IllegalArgumentException("XP a distribuir deve ser positivo");
        }
        double xpPorAtributo = xpTotal / atributos.size();
        for (var atributo : atributos) {
            atributo.adicionarXp(xpPorAtributo);
        }
        this.overall = calcularOverall();
        this.ultimaSincronizacao = LocalDateTime.now();
    }

    public double calcularOverall() {
        double somaValoresPonderados = 0;
        double somaPesos = 0;
        for (var atributo : atributos) {
            somaValoresPonderados += atributo.getValor() * atributo.getPeso();
            somaPesos += atributo.getPeso();
        }
        return somaPesos > 0 ? somaValoresPonderados / somaPesos : 0;
    }

    public List<AtributoEsportivo> filtrarAtributosPorEsporte(String tipoEsporte) {
        notNull(tipoEsporte, "O tipo de esporte nao pode ser nulo");
        return atributos.stream()
            .filter(a -> a.getTipoEsporte().equalsIgnoreCase(tipoEsporte))
            .toList();
    }
}

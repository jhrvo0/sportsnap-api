package com.sportsnap.gamification.dominio.desafio;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Criterio mensuravel de conclusao de um desafio (RN15): define o tipo de
 * medicao e uma meta-alvo positiva. Para LIMIAR_ATRIBUTO, indica o atributo alvo.
 */
public class CriterioDesafio {

    private final TipoCriterio tipo;
    private final int meta;
    private final String alvoAtributo;

    public CriterioDesafio(TipoCriterio tipo, int meta, String alvoAtributo) {
        notNull(tipo, "O tipo do criterio nao pode ser nulo");
        isTrue(meta > 0, "A meta do criterio deve ser positiva");
        if (tipo == TipoCriterio.LIMIAR_ATRIBUTO) {
            isTrue(alvoAtributo != null && !alvoAtributo.isBlank(),
                "LIMIAR_ATRIBUTO exige o atributo alvo");
        }
        this.tipo = tipo;
        this.meta = meta;
        this.alvoAtributo = alvoAtributo;
    }

    public static CriterioDesafio de(TipoCriterio tipo, int meta) {
        return new CriterioDesafio(tipo, meta, null);
    }

    public TipoCriterio getTipo() {
        return tipo;
    }

    public int getMeta() {
        return meta;
    }

    public String getAlvoAtributo() {
        return alvoAtributo;
    }
}

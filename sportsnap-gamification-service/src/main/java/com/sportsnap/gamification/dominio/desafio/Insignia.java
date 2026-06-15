package com.sportsnap.gamification.dominio.desafio;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Insignia concedida ao perfil do atleta pela conclusao de um desafio (RN17,
 * RN29). Pertence a este contexto e nunca escreve na Carta, preservando o
 * isolamento.
 */
public class Insignia {

    private final Integer id;
    private final AtletaId atletaId;
    private final String codigo;
    private final int desafioId;
    private final LocalDateTime concedidaEm;

    public Insignia(AtletaId atletaId, String codigo, int desafioId, LocalDateTime concedidaEm) {
        this(null, atletaId, codigo, desafioId, concedidaEm);
    }

    public Insignia(Integer id, AtletaId atletaId, String codigo, int desafioId,
                    LocalDateTime concedidaEm) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notBlank(codigo, "O codigo da insignia e obrigatorio");
        notNull(concedidaEm, "A data de concessao nao pode ser nula");
        this.id = id;
        this.atletaId = atletaId;
        this.codigo = codigo;
        this.desafioId = desafioId;
        this.concedidaEm = concedidaEm;
    }

    public Integer getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getDesafioId() {
        return desafioId;
    }

    public LocalDateTime getConcedidaEm() {
        return concedidaEm;
    }
}

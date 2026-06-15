package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Temporada competitiva de uma modalidade (RN36 a RN40). Possui periodo valido,
 * pode ser cancelada antes de iniciar (RN38) e, ao encerrar, congela um snapshot
 * final imutavel (RN39).
 */
public class Temporada {

    private final Integer id;
    private final String modalidade;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;
    private StatusTemporada status;
    private final List<EntradaSnapshot> snapshotFinal;

    public Temporada(String modalidade, LocalDateTime inicio, LocalDateTime fim) {
        this(null, modalidade, inicio, fim, StatusTemporada.AGENDADA, List.of());
    }

    public Temporada(Integer id, String modalidade, LocalDateTime inicio, LocalDateTime fim,
                     StatusTemporada status, List<EntradaSnapshot> snapshotFinal) {
        notBlank(modalidade, "A modalidade da temporada e obrigatoria");
        notNull(inicio, "O inicio da temporada nao pode ser nulo");
        notNull(fim, "O fim da temporada nao pode ser nulo");
        isTrue(fim.isAfter(inicio), "RN36: o fim deve ser posterior ao inicio");
        notNull(status, "O status da temporada nao pode ser nulo");
        this.id = id;
        this.modalidade = modalidade;
        this.inicio = inicio;
        this.fim = fim;
        this.status = status;
        this.snapshotFinal = new ArrayList<>(snapshotFinal == null ? List.of() : snapshotFinal);
    }

    public Integer getId() {
        return id;
    }

    public String getModalidade() {
        return modalidade;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public StatusTemporada getStatus() {
        return status;
    }

    public List<EntradaSnapshot> getSnapshotFinal() {
        return Collections.unmodifiableList(snapshotFinal);
    }

    public boolean estaAtivaEm(LocalDateTime instante) {
        notNull(instante, "O instante nao pode ser nulo");
        if (status == StatusTemporada.CANCELADA || status == StatusTemporada.ENCERRADA) {
            return false;
        }
        return !instante.isBefore(inicio) && !instante.isAfter(fim);
    }

    public boolean sobrepoeA(Temporada outra) {
        if (!modalidade.equalsIgnoreCase(outra.modalidade)) {
            return false;
        }
        return inicio.isBefore(outra.fim) && outra.inicio.isBefore(fim);
    }

    /** Cancelamento so e permitido antes do inicio (RN38). */
    public void cancelar(LocalDateTime agora) {
        notNull(agora, "O instante atual nao pode ser nulo");
        if (!agora.isBefore(inicio)) {
            throw new IllegalStateException("RN38: temporada so pode ser cancelada antes de iniciar");
        }
        this.status = StatusTemporada.CANCELADA;
    }

    /** Encerra a temporada congelando o snapshot final imutavel (RN39). */
    public void encerrar(List<EntradaSnapshot> snapshot) {
        notNull(snapshot, "O snapshot nao pode ser nulo");
        if (status == StatusTemporada.ENCERRADA) {
            throw new IllegalStateException("Temporada ja encerrada");
        }
        if (status == StatusTemporada.CANCELADA) {
            throw new IllegalStateException("Temporada cancelada nao pode ser encerrada");
        }
        this.snapshotFinal.clear();
        this.snapshotFinal.addAll(snapshot);
        this.status = StatusTemporada.ENCERRADA;
    }
}

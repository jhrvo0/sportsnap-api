package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.notBlank;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckInId;
import java.time.LocalDateTime;

public class RegistroAtividade {

    private final RegistroAtividadeId id;
    private final AtletaId atletaId;
    private final CheckInId checkInId;
    private final String esporte;
    private final LocalDateTime data;
    private final double distancia;
    private final long duracaoSegundos;
    private final Intensidade intensidade;
    private final double xpCalculado;
    private final Integer esforcePercebido; // 1 to 10
    private final String observacoes;
    private final String origemRegistro; // MANUAL, CHECKIN, IMPORTADO
    private final String metricas; // serialized JSON or other text
    private final LocalDateTime criadoEm;
    private final LocalDateTime atualizadoEm;

    // Construtor completo/principal
    public RegistroAtividade(RegistroAtividadeId id, AtletaId atletaId, CheckInId checkInId,
                             String esporte, LocalDateTime data, double distancia,
                             long duracaoSegundos, Intensidade intensidade, double xpCalculado,
                             Integer esforcePercebido, String observacoes, String origemRegistro,
                             String metricas, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notBlank(esporte, "O esporte nao pode estar em branco");
        notNull(data, "A data nao pode ser nula");
        isTrue(distancia >= 0, "A distancia nao pode ser negativa");
        isTrue(duracaoSegundos > 0, "A duracao deve ser positiva");
        notBlank(origemRegistro, "A origem do registro nao pode estar em branco");
        if (esforcePercebido != null) {
            isTrue(esforcePercebido >= 1 && esforcePercebido <= 10, "O esforco percebido deve ser de 1 a 10");
        }

        this.id = id;
        this.atletaId = atletaId;
        this.checkInId = checkInId;
        this.esporte = esporte;
        this.data = data;
        this.distancia = distancia;
        this.duracaoSegundos = duracaoSegundos;
        this.intensidade = intensidade;
        this.xpCalculado = xpCalculado;
        this.esforcePercebido = esforcePercebido;
        this.observacoes = observacoes;
        this.origemRegistro = origemRegistro;
        this.metricas = metricas;
        this.criadoEm = criadoEm != null ? criadoEm : LocalDateTime.now();
        this.atualizadoEm = atualizadoEm != null ? atualizadoEm : LocalDateTime.now();
    }

    // Construtor legado 1
    public RegistroAtividade(CheckInId checkInId, double distancia, long duracaoSegundos,
                             Intensidade intensidade) {
        this(null, new AtletaId(1), checkInId, "CORRIDA", LocalDateTime.now(), distancia, duracaoSegundos,
             intensidade, distancia * intensidade.getMultiplicador(), null, null, "CHECKIN", null,
             LocalDateTime.now(), LocalDateTime.now());
    }

    // Construtor legado 2
    public RegistroAtividade(RegistroAtividadeId id, CheckInId checkInId, double distancia,
                             long duracaoSegundos, Intensidade intensidade, double xpCalculado) {
        this(id, new AtletaId(1), checkInId, "CORRIDA", LocalDateTime.now(), distancia, duracaoSegundos,
             intensidade, xpCalculado, null, null, "CHECKIN", null,
             LocalDateTime.now(), LocalDateTime.now());
    }

    public RegistroAtividadeId getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public CheckInId getCheckInId() {
        return checkInId;
    }

    public String getEsporte() {
        return esporte;
    }

    public LocalDateTime getData() {
        return data;
    }

    public double getDistancia() {
        return distancia;
    }

    public long getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public Intensidade getIntensidade() {
        return intensidade;
    }

    public double getXpCalculado() {
        return xpCalculado;
    }

    public Integer getEsforcoPercebido() {
        return esforcePercebido;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public String getOrigemRegistro() {
        return origemRegistro;
    }

    public String getMetricas() {
        return metricas;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public Double getRitmoMedio() {
        if (distancia > 0 && duracaoSegundos > 0) {
            return (duracaoSegundos / 60.0) / distancia;
        }
        return null;
    }

    public Double getCaloriasEstimadas() {
        if ("CORRIDA".equalsIgnoreCase(esporte)) {
            return distancia * 75.0; // ~75 kcal per km
        } else if ("BICICLETA".equalsIgnoreCase(esporte) || "CICLISMO".equalsIgnoreCase(esporte)) {
            return distancia * 35.0; // ~35 kcal per km
        } else {
            return (duracaoSegundos / 60.0) * 8.0; // ~8 kcal per minute
        }
    }
}

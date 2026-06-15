package com.sportsnap.marketplace.dominio.assinatura;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

public class Cota {

    private final LocalDateTime dataExpiracao;

    public Cota(LocalDateTime dataExpiracao) {
        notNull(dataExpiracao, "A data de expiracao da cota nao pode ser nula");
        this.dataExpiracao = dataExpiracao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public boolean isExpirada(LocalDateTime agora) {
        return agora.isAfter(dataExpiracao);
    }
}

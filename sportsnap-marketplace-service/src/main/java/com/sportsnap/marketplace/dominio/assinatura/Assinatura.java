package com.sportsnap.marketplace.dominio.assinatura;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;

public class Assinatura {

    private AssinaturaId id;
    private AtletaId atletaId;
    private List<Cota> cotas;
    private LocalDateTime dataInicioCiclo;
    private LocalDateTime dataFimCiclo;
    private AssinaturaStatus status;
    private boolean autoRenovacao;

    // Construtor JPA
    protected Assinatura() {}

    public Assinatura(AtletaId atletaId, int cotaInicial) {
        this.id = AssinaturaId.gerar();
        this.atletaId = atletaId;
        this.dataInicioCiclo = LocalDateTime.now();
        this.dataFimCiclo = this.dataInicioCiclo.plusMonths(1);
        this.status = AssinaturaStatus.ATIVA;
        this.autoRenovacao = true;
        this.cotas = new ArrayList<>();
        adicionarCotas(cotaInicial, this.dataFimCiclo.plusMonths(1));
    }

    private void adicionarCotas(int quantidade, LocalDateTime dataExpiracao) {
        for (int i = 0; i < quantidade; i++) {
            this.cotas.add(new Cota(dataExpiracao));
        }
    }

    public AssinaturaId getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public int getSaldoCotas() {
        limparCotasExpiradas();
        return cotas.size();
    }

    public LocalDateTime getDataFimCiclo() {
        return dataFimCiclo;
    }

    public AssinaturaStatus getStatus() {
        return status;
    }

    public boolean isAtiva() {
        return this.status == AssinaturaStatus.ATIVA || this.status == AssinaturaStatus.CANCELADA_PENDENTE;
    }

    public boolean possuiCota() {
        limparCotasExpiradas();
        return this.isAtiva() && !this.cotas.isEmpty();
    }

    public void consumirCota() {
        limparCotasExpiradas();
        if (!isAtiva()) {
            throw new IllegalStateException("Assinatura nao esta ativa.");
        }
        if (this.cotas.isEmpty()) {
            throw new IllegalStateException("Saldo de cotas insuficiente.");
        }
        
        // FIFO: Ordenar pela data de expiração mais próxima e remover a primeira
        this.cotas.sort(Comparator.comparing(Cota::getDataExpiracao));
        this.cotas.remove(0);
    }

    public void restituirCota() {
        if (!isAtiva()) {
            throw new IllegalStateException("Assinatura nao esta ativa.");
        }
        // Restitui uma cota com validade até o fim do próximo ciclo
        this.cotas.add(new Cota(this.dataFimCiclo.plusMonths(1)));
    }

    private void limparCotasExpiradas() {
        if (this.cotas != null) {
            LocalDateTime agora = LocalDateTime.now();
            this.cotas.removeIf(cota -> cota.isExpirada(agora));
        }
    }

    public void cancelarAutoRenovacao() {
        if (this.status != AssinaturaStatus.ATIVA) {
            throw new IllegalStateException("Apenas assinaturas ativas podem ser canceladas.");
        }
        this.status = AssinaturaStatus.CANCELADA_PENDENTE;
        this.autoRenovacao = false;
    }

    public void renovarCiclo(int limiteRollover, int franquiaMensal) {
        if (this.status == AssinaturaStatus.INATIVA) {
            throw new IllegalStateException("Assinatura inativa nao pode ser renovada.");
        }
        if (this.status == AssinaturaStatus.CANCELADA_PENDENTE) {
            this.status = AssinaturaStatus.INATIVA;
            this.cotas.clear();
            return;
        }

        limparCotasExpiradas();

        // RN 4: Rollover limitado
        if (this.cotas.size() > limiteRollover) {
            // Se excedeu o limite, remove as mais antigas até atingir o limite
            this.cotas.sort(Comparator.comparing(Cota::getDataExpiracao));
            int paraRemover = this.cotas.size() - limiteRollover;
            for (int i = 0; i < paraRemover; i++) {
                this.cotas.remove(0);
            }
        }

        this.dataInicioCiclo = this.dataFimCiclo;
        this.dataFimCiclo = this.dataInicioCiclo.plusMonths(1);
        
        // As novas cotas expiram no próximo ciclo (daqui a 2 ciclos)
        adicionarCotas(franquiaMensal, this.dataFimCiclo.plusMonths(1));
    }
}

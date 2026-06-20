package com.sportsnap.gamification.dominio.conexao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class PedidoConexao {

    private final PedidoConexaoId id;
    private final PerfilId solicitanteId;
    private final PerfilId alvoId;
    private final LocalDateTime criadoEm;
    private StatusPedido status;

    public PedidoConexao(PerfilId solicitanteId, PerfilId alvoId) {
        notNull(solicitanteId, "O solicitanteId do PedidoConexao nao pode ser nulo");
        notNull(alvoId, "O alvoId do PedidoConexao nao pode ser nulo");
        this.id = null;
        this.solicitanteId = solicitanteId;
        this.alvoId = alvoId;
        this.criadoEm = LocalDateTime.now();
        this.status = StatusPedido.PENDENTE;
    }

    public PedidoConexao(PedidoConexaoId id, PerfilId solicitanteId, PerfilId alvoId,
                         LocalDateTime criadoEm, StatusPedido status) {
        notNull(id, "O id do PedidoConexao nao pode ser nulo");
        notNull(solicitanteId, "O solicitanteId do PedidoConexao nao pode ser nulo");
        notNull(alvoId, "O alvoId do PedidoConexao nao pode ser nulo");
        this.id = id;
        this.solicitanteId = solicitanteId;
        this.alvoId = alvoId;
        this.criadoEm = criadoEm;
        this.status = status;
    }

    public PedidoConexaoId getId() {
        return id;
    }

    public PerfilId getSolicitanteId() {
        return solicitanteId;
    }

    public PerfilId getAlvoId() {
        return alvoId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public boolean isPendente() {
        return status == StatusPedido.PENDENTE;
    }

    public void aprovar() {
        if (!isPendente()) {
            throw new IllegalStateException("Apenas pedidos pendentes podem ser aprovados");
        }
        this.status = StatusPedido.APROVADO;
    }

    public void recusar() {
        if (!isPendente()) {
            throw new IllegalStateException("Apenas pedidos pendentes podem ser recusados");
        }
        this.status = StatusPedido.RECUSADO;
    }

    public void cancelar() {
        if (!isPendente()) {
            throw new IllegalStateException("Apenas pedidos pendentes podem ser cancelados");
        }
        this.status = StatusPedido.CANCELADO;
    }
}

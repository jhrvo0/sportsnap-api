package com.sportsnap.gamification.dominio.conexao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class Conexao {

    private final ConexaoId id;
    private final PerfilId seguidorId;
    private final PerfilId seguidoId;
    private final LocalDateTime criadaEm;

    public Conexao(PerfilId seguidorId, PerfilId seguidoId) {
        notNull(seguidorId, "O seguidorId da Conexao nao pode ser nulo");
        notNull(seguidoId, "O seguidoId da Conexao nao pode ser nulo");
        this.id = null;
        this.seguidorId = seguidorId;
        this.seguidoId = seguidoId;
        this.criadaEm = LocalDateTime.now();
    }

    public Conexao(ConexaoId id, PerfilId seguidorId, PerfilId seguidoId, LocalDateTime criadaEm) {
        notNull(id, "O id da Conexao nao pode ser nulo");
        notNull(seguidorId, "O seguidorId da Conexao nao pode ser nulo");
        notNull(seguidoId, "O seguidoId da Conexao nao pode ser nulo");
        notNull(criadaEm, "O criadaEm da Conexao nao pode ser nulo");
        this.id = id;
        this.seguidorId = seguidorId;
        this.seguidoId = seguidoId;
        this.criadaEm = criadaEm;
    }

    public ConexaoId getId() {
        return id;
    }

    public PerfilId getSeguidorId() {
        return seguidorId;
    }

    public PerfilId getSeguidoId() {
        return seguidoId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    @Override
    public String toString() {
        return seguidorId + " -> " + seguidoId;
    }
}

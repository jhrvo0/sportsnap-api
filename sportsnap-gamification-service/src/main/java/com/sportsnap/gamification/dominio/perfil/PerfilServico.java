package com.sportsnap.gamification.dominio.perfil;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

public class PerfilServico {

    private final PerfilRepositorio repositorio;

    public PerfilServico(PerfilRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Perfil nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Perfil criar(AtletaId usuarioId, String nomeExibicao, TipoConta tipoConta) {
        notNull(usuarioId, "O usuarioId nao pode ser nulo");
        var jaExiste = repositorio.obterPorUsuario(usuarioId);
        if (jaExiste.isPresent()) {
            throw new IllegalStateException("Ja existe um Perfil para esse usuario: " + usuarioId);
        }
        return repositorio.salvar(new Perfil(usuarioId, nomeExibicao, tipoConta));
    }

    public Perfil editar(PerfilId id, AtletaId solicitanteId, String nomeExibicao,
                         String bio, String esporte, String localidade, Visibilidade visibilidade) {
        notNull(id, "O id do Perfil nao pode ser nulo");
        notNull(solicitanteId, "O solicitanteId nao pode ser nulo");
        var perfil = repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Perfil nao encontrado: " + id));
        if (!perfil.getUsuarioId().equals(solicitanteId)) {
            throw new IllegalStateException("Apenas o titular pode editar o Perfil");
        }
        perfil.setNomeExibicao(nomeExibicao);
        perfil.setBio(bio);
        perfil.setEsporte(esporte);
        perfil.setLocalidade(localidade);
        if (visibilidade != null) {
            perfil.setVisibilidade(visibilidade);
        }
        return repositorio.salvar(perfil);
    }

    public Perfil obter(PerfilId id) {
        notNull(id, "O id do Perfil nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Perfil nao encontrado: " + id));
    }

    public Perfil obterPorUsuario(AtletaId usuarioId) {
        notNull(usuarioId, "O usuarioId nao pode ser nulo");
        return repositorio.obterPorUsuario(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Perfil nao encontrado para usuario: " + usuarioId));
    }
}

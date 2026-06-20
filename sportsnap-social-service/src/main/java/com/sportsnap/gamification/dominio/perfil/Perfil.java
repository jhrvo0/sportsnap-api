package com.sportsnap.gamification.dominio.perfil;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

public class Perfil {

    private final PerfilId id;
    private final AtletaId usuarioId;
    private String nomeExibicao;
    private TipoConta tipoConta;
    private String bio;
    private String esporte;
    private String localidade;
    private Visibilidade visibilidade;
    private int totalSeguidores;
    private int totalSeguindo;
    private String fotoPerfil;

    public Perfil(AtletaId usuarioId, String nomeExibicao, TipoConta tipoConta) {
        notNull(usuarioId, "O usuarioId do Perfil nao pode ser nulo");
        this.id = null;
        this.usuarioId = usuarioId;
        setNomeExibicao(nomeExibicao);
        setTipoConta(tipoConta);
        this.visibilidade = Visibilidade.PUBLICA;
        this.totalSeguidores = 0;
        this.totalSeguindo = 0;
    }

    public Perfil(PerfilId id, AtletaId usuarioId, String nomeExibicao, TipoConta tipoConta,
                  String bio, String esporte, String localidade, Visibilidade visibilidade,
                  int totalSeguidores, int totalSeguindo, String fotoPerfil) {
        notNull(id, "O id do Perfil nao pode ser nulo");
        notNull(usuarioId, "O usuarioId do Perfil nao pode ser nulo");
        this.id = id;
        this.usuarioId = usuarioId;
        setNomeExibicao(nomeExibicao);
        setTipoConta(tipoConta);
        setBio(bio);
        this.esporte = esporte;
        this.localidade = localidade;
        this.visibilidade = visibilidade != null ? visibilidade : Visibilidade.PUBLICA;
        this.totalSeguidores = totalSeguidores;
        this.totalSeguindo = totalSeguindo;
        this.fotoPerfil = fotoPerfil;
    }

    public PerfilId getId() { return id; }
    public AtletaId getUsuarioId() { return usuarioId; }

    public String getNomeExibicao() { return nomeExibicao; }
    public void setNomeExibicao(String nomeExibicao) {
        notNull(nomeExibicao, "O nomeExibicao do Perfil nao pode ser nulo");
        notBlank(nomeExibicao, "O nomeExibicao do Perfil nao pode estar em branco");
        this.nomeExibicao = nomeExibicao;
    }

    public TipoConta getTipoConta() { return tipoConta; }
    public void setTipoConta(TipoConta tipoConta) {
        notNull(tipoConta, "O tipoConta do Perfil nao pode ser nulo");
        this.tipoConta = tipoConta;
    }

    public String getBio() { return bio; }
    public void setBio(String bio) {
        if (bio != null) {
            isTrue(bio.length() <= 300, "A bio nao pode ter mais de 300 caracteres");
        }
        this.bio = bio;
    }

    public String getEsporte() { return esporte; }
    public void setEsporte(String esporte) { this.esporte = esporte; }

    public String getLocalidade() { return localidade; }
    public void setLocalidade(String localidade) { this.localidade = localidade; }

    public Visibilidade getVisibilidade() { return visibilidade; }
    public void setVisibilidade(Visibilidade visibilidade) {
        notNull(visibilidade, "A visibilidade do Perfil nao pode ser nula");
        this.visibilidade = visibilidade;
    }

    public boolean isPublico() { return visibilidade == Visibilidade.PUBLICA; }

    public int getTotalSeguidores() { return totalSeguidores; }
    public int getTotalSeguindo() { return totalSeguindo; }

    public void incrementarSeguidores() { this.totalSeguidores++; }
    public void decrementarSeguidores() { if (totalSeguidores > 0) this.totalSeguidores--; }
    public void incrementarSeguindo() { this.totalSeguindo++; }
    public void decrementarSeguindo() { if (totalSeguindo > 0) this.totalSeguindo--; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    @Override
    public String toString() { return nomeExibicao; }
}

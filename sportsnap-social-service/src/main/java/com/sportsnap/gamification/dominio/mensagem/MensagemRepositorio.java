package com.sportsnap.gamification.dominio.mensagem;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface MensagemRepositorio {

    Mensagem salvar(Mensagem mensagem);

    Optional<Mensagem> obter(MensagemId id);

    List<Mensagem> listarConversa(PerfilId perfilId1, PerfilId perfilId2);

    List<Mensagem> listarEnvolvendo(PerfilId perfilId);

    int contarNaoLidas(PerfilId destinatarioId);

    void limpar();
}

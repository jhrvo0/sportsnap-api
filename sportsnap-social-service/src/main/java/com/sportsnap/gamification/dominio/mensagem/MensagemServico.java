package com.sportsnap.gamification.dominio.mensagem;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@Service
public class MensagemServico {

    private final MensagemRepositorio repositorio;

    public MensagemServico(MensagemRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Mensagem nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Mensagem enviar(PerfilId remetenteId, PerfilId destinatarioId, String conteudo) {
        notNull(remetenteId,    "O remetenteId nao pode ser nulo");
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        return repositorio.salvar(new Mensagem(remetenteId, destinatarioId, conteudo));
    }

    public List<Mensagem> listarConversa(PerfilId perfilId1, PerfilId perfilId2) {
        notNull(perfilId1, "O perfilId1 nao pode ser nulo");
        notNull(perfilId2, "O perfilId2 nao pode ser nulo");
        return repositorio.listarConversa(perfilId1, perfilId2);
    }

    public List<Mensagem> listarUltimasMensagensPorConversa(PerfilId meuId) {
        notNull(meuId, "O meuId nao pode ser nulo");
        var todas = repositorio.listarEnvolvendo(meuId);
        // Agrupa por "outro participante" e pega a mensagem mais recente de cada
        Map<Integer, Mensagem> porOutro = new LinkedHashMap<>();
        todas.stream()
            .sorted(Comparator.comparing(Mensagem::getCriadaEm).reversed())
            .forEach(m -> {
                int outroId = m.outroParticipante(meuId).getId();
                porOutro.putIfAbsent(outroId, m);
            });
        return List.copyOf(porOutro.values());
    }

    public void marcarComoLida(MensagemId id, PerfilId destinatarioId) {
        notNull(id,             "O id da Mensagem nao pode ser nulo");
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        var mensagem = repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Mensagem nao encontrada: " + id));
        if (!mensagem.getDestinatarioId().equals(destinatarioId)) {
            throw new IllegalStateException("Apenas o destinatario pode marcar a mensagem como lida");
        }
        mensagem.marcarComoLida();
        repositorio.salvar(mensagem);
    }

    public int contarNaoLidas(PerfilId destinatarioId) {
        notNull(destinatarioId, "O destinatarioId nao pode ser nulo");
        return repositorio.contarNaoLidas(destinatarioId);
    }
}

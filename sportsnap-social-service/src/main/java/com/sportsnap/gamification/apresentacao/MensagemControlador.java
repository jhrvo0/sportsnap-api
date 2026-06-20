package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.mensagem.Mensagem;
import com.sportsnap.gamification.dominio.mensagem.MensagemId;
import com.sportsnap.gamification.dominio.mensagem.MensagemServico;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@RestController
@RequestMapping("/api/mensagens")
public class MensagemControlador {

    @Autowired private MensagemServico mensagemServico;

    @PostMapping
    public Mensagem enviar(@RequestBody MensagemDto dto) {
        return mensagemServico.enviar(
            new PerfilId(dto.remetenteId),
            new PerfilId(dto.destinatarioId),
            dto.conteudo);
    }

    @GetMapping("/conversa")
    public List<Mensagem> conversa(@RequestParam int perfilId1, @RequestParam int perfilId2) {
        return mensagemServico.listarConversa(new PerfilId(perfilId1), new PerfilId(perfilId2));
    }

    @GetMapping("/inbox")
    public List<Mensagem> inbox(@RequestParam int perfilId) {
        return mensagemServico.listarUltimasMensagensPorConversa(new PerfilId(perfilId));
    }

    @GetMapping("/nao-lidas")
    public int naoLidas(@RequestParam int perfilId) {
        return mensagemServico.contarNaoLidas(new PerfilId(perfilId));
    }

    @PostMapping("/{id}/lida")
    public void marcarLida(@PathVariable int id, @RequestParam int destinatarioId) {
        mensagemServico.marcarComoLida(new MensagemId(id), new PerfilId(destinatarioId));
    }

    public static class MensagemDto {
        public int    remetenteId;
        public int    destinatarioId;
        public String conteudo;
    }
}

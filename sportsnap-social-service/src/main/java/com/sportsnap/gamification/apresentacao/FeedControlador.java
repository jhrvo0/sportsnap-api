package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.feed.Curtida;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.notificacao.Notificacao;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoServico;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@RestController
@RequestMapping("/api/feed")
public class FeedControlador {

    @Autowired private FeedServico feedServico;
    @Autowired private NotificacaoServico notificacaoServico;

    @GetMapping("/{perfilId}")
    public List<ItemFeed> consultarFeed(@PathVariable int perfilId,
                                         @RequestParam(defaultValue = "0") int pagina) {
        return feedServico.consultarFeed(new PerfilId(perfilId), pagina);
    }

    @PostMapping("/{itemId}/curtir")
    public Curtida curtir(@PathVariable int itemId, @RequestBody CurtidaDto dto) {
        return feedServico.curtir(new PerfilId(dto.usuarioId), new ItemFeedId(itemId));
    }

    @DeleteMapping("/{itemId}/curtir")
    public void descurtir(@PathVariable int itemId, @RequestBody CurtidaDto dto) {
        feedServico.descurtir(new PerfilId(dto.usuarioId), new ItemFeedId(itemId));
    }

    @GetMapping("/{perfilId}/notificacoes")
    public List<Notificacao> notificacoes(@PathVariable int perfilId) {
        return notificacaoServico.listar(new PerfilId(perfilId));
    }

    @GetMapping("/{perfilId}/notificacoes/nao-lidas")
    public int contarNaoLidas(@PathVariable int perfilId) {
        return notificacaoServico.contarNaoLidas(new PerfilId(perfilId));
    }

    @PostMapping("/{perfilId}/notificacoes/marcar-lidas")
    public void marcarTodasComoLidas(@PathVariable int perfilId) {
        notificacaoServico.marcarTodasComoLidas(new PerfilId(perfilId));
    }

    @GetMapping("/autor/{perfilId}")
    public List<ItemFeed> listarPorAutor(@PathVariable int perfilId) {
        return feedServico.listarPorAutor(new PerfilId(perfilId));
    }

    @PostMapping("/publicar")
    public ItemFeed publicar(@RequestBody PublicacaoDto dto) {
        return feedServico.publicarItem(new PerfilId(dto.autorId),
            com.sportsnap.gamification.dominio.feed.TipoItemFeed.valueOf(dto.tipo), dto.referenciaId);
    }

    public static class PublicacaoDto {
        public int    autorId;
        public String tipo;
        public int    referenciaId;
    }

    public static class CurtidaDto {
        public int usuarioId;
    }
}

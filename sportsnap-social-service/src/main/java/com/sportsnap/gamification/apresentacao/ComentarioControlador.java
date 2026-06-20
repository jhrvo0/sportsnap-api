package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.comentario.Comentario;
import com.sportsnap.gamification.dominio.comentario.ComentarioId;
import com.sportsnap.gamification.dominio.comentario.ComentarioServico;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioControlador {

    @Autowired private ComentarioServico servico;

    @PostMapping
    public Comentario criar(@RequestBody ComentarioDto dto) {
        var itemId = new ItemFeedId(dto.itemFeedId);
        var autorId = new PerfilId(dto.autorId);
        if (dto.parentId != null) {
            return servico.responder(itemId, autorId, dto.conteudo, new ComentarioId(dto.parentId));
        }
        return servico.comentar(itemId, autorId, dto.conteudo);
    }

    @GetMapping
    public List<Comentario> listar(@RequestParam int itemId) {
        return servico.listarPorItem(new ItemFeedId(itemId));
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable int id, @RequestParam int autorId) {
        servico.remover(new ComentarioId(id), new PerfilId(autorId));
    }

    public static class ComentarioDto {
        public int     itemFeedId;
        public int     autorId;
        public String  conteudo;
        public Integer parentId;
    }
}

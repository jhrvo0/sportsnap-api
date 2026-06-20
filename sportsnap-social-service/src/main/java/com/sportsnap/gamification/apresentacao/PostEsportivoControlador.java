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

import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.post.PostEsportivo;
import com.sportsnap.gamification.dominio.post.PostEsportivoId;
import com.sportsnap.gamification.dominio.post.PostEsportivoServico;

@RestController
@RequestMapping("/api/posts")
public class PostEsportivoControlador {

    @Autowired private PostEsportivoServico servico;

    @PostMapping
    public PostEsportivo criar(@RequestBody PostDto dto) {
        return servico.criar(new PerfilId(dto.autorId), dto.conteudo, dto.esporte);
    }

    @GetMapping("/{id}")
    public PostEsportivo obter(@PathVariable int id) {
        return servico.obter(new PostEsportivoId(id));
    }

    @GetMapping
    public List<PostEsportivo> listarPorAutor(@RequestParam int autorId) {
        return servico.listarPorAutor(new PerfilId(autorId));
    }

    public static class PostDto {
        public int    autorId;
        public String conteudo;
        public String esporte;
    }
}

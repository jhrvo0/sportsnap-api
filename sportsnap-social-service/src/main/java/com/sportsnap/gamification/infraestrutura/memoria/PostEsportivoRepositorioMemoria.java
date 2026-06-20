package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.post.PostEsportivo;
import com.sportsnap.gamification.dominio.post.PostEsportivoId;
import com.sportsnap.gamification.dominio.post.PostEsportivoRepositorio;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class PostEsportivoRepositorioMemoria implements PostEsportivoRepositorio {

    private final Map<PostEsportivoId, PostEsportivo> dados = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(0);

    @Override
    public PostEsportivo salvar(PostEsportivo post) {
        if (post.getId() == null) {
            var novo = new PostEsportivo(
                new PostEsportivoId(sequencia.incrementAndGet()),
                post.getAutorId(),
                post.getConteudo(),
                post.getEsporte(),
                post.getCriadoEm()
            );
            dados.put(novo.getId(), novo);
            return novo;
        }
        dados.put(post.getId(), post);
        return post;
    }

    @Override
    public Optional<PostEsportivo> obter(PostEsportivoId id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<PostEsportivo> listarPorAutor(PerfilId autorId) {
        return dados.values().stream()
            .filter(p -> p.getAutorId().equals(autorId))
            .toList();
    }

    @Override
    public void limpar() {
        dados.clear();
        sequencia.set(0);
    }
}

package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.post.PostEsportivo;
import com.sportsnap.gamification.dominio.post.PostEsportivoId;
import com.sportsnap.gamification.dominio.post.PostEsportivoRepositorio;

@Entity
@Table(name = "POST_ESPORTIVO")
class PostEsportivoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "AUTOR_ID")
    int autorId;

    @Column(length = 500)
    String conteudo;

    String esporte;

    @Column(name = "CRIADO_EM")
    LocalDateTime criadoEm;
}

interface PostEsportivoJpaRepository extends JpaRepository<PostEsportivoJpa, Integer> {
    List<PostEsportivoJpa> findByAutorIdOrderByCriadoEmDesc(int autorId);
}

@Repository
class PostEsportivoRepositorioImpl implements PostEsportivoRepositorio {

    @Autowired PostEsportivoJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public PostEsportivo salvar(PostEsportivo post) {
        return mapeador.paraDominio(repositorio.save(mapeador.paraJpa(post)));
    }

    @Override
    public Optional<PostEsportivo> obter(PostEsportivoId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<PostEsportivo> listarPorAutor(PerfilId autorId) {
        return repositorio.findByAutorIdOrderByCriadoEmDesc(autorId.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

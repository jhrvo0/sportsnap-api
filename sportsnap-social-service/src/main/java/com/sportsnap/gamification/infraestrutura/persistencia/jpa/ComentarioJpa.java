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

import com.sportsnap.gamification.dominio.comentario.Comentario;
import com.sportsnap.gamification.dominio.comentario.ComentarioId;
import com.sportsnap.gamification.dominio.comentario.ComentarioRepositorio;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;

@Entity
@Table(name = "COMENTARIO_FEED")
class ComentarioJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "ITEM_FEED_ID")
    int itemFeedId;

    @Column(name = "AUTOR_ID")
    int autorId;

    @Column(length = 300)
    String conteudo;

    @Column(name = "PARENT_ID")
    Integer parentId;

    @Column(name = "CRIADO_EM")
    LocalDateTime criadoEm;
}

interface ComentarioJpaRepository extends JpaRepository<ComentarioJpa, Integer> {
    List<ComentarioJpa> findByItemFeedIdOrderByCriadoEmAsc(int itemFeedId);
    List<ComentarioJpa> findByParentIdOrderByCriadoEmAsc(int parentId);
}

@Repository
class ComentarioRepositorioImpl implements ComentarioRepositorio {

    @Autowired ComentarioJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public Comentario salvar(Comentario c) {
        return mapeador.paraDominio(repositorio.save(mapeador.paraJpa(c)));
    }

    @Override
    public Optional<Comentario> obter(ComentarioId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Comentario> listarPorItem(ItemFeedId itemFeedId) {
        return repositorio.findByItemFeedIdOrderByCriadoEmAsc(itemFeedId.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<Comentario> listarRespostasPorParent(ComentarioId parentId) {
        return repositorio.findByParentIdOrderByCriadoEmAsc(parentId.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public void remover(ComentarioId id) {
        repositorio.deleteById(id.getId());
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

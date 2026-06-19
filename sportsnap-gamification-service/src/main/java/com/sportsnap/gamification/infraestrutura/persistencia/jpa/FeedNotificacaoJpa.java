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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.conexao.PedidoConexaoId;
import com.sportsnap.gamification.dominio.feed.Curtida;
import com.sportsnap.gamification.dominio.feed.CurtidaId;
import com.sportsnap.gamification.dominio.feed.CurtidaRepositorio;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.feed.ItemFeedRepositorio;
import com.sportsnap.gamification.dominio.notificacao.Notificacao;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoId;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoRepositorio;
import com.sportsnap.gamification.dominio.notificacao.TipoNotificacao;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@Entity
@Table(name = "ITEM_FEED")
class ItemFeedJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "AUTOR_ID")
    int autorId;

    String tipo;

    @Column(name = "REFERENCIA_ID")
    int referenciaId;

    @Column(name = "PUBLICADO_EM")
    LocalDateTime publicadoEm;
}

interface ItemFeedJpaRepository extends JpaRepository<ItemFeedJpa, Integer> {
    List<ItemFeedJpa> findByAutorIdIn(List<Integer> autorIds);
}

@Entity
@Table(name = "CURTIDA_FEED")
class CurtidaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "USUARIO_ID")
    int usuarioId;

    @Column(name = "ITEM_ID")
    int itemId;
}

interface CurtidaJpaRepository extends JpaRepository<CurtidaJpa, Integer> {
    Optional<CurtidaJpa> findByUsuarioIdAndItemId(int usuarioId, int itemId);
    int countByItemId(int itemId);
}

@Entity
@Table(name = "NOTIFICACAO_SOCIAL")
class NotificacaoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "DESTINATARIO_ID")
    int destinatarioId;

    String tipo;

    @Column(name = "REFERENCIA_ID")
    int referenciaId;

    boolean lida;

    @Column(name = "CRIADA_EM")
    LocalDateTime criadaEm;

    @Column(name = "NUM_ATORES")
    int numAtores;
}

interface NotificacaoJpaRepository extends JpaRepository<NotificacaoJpa, Integer> {
    List<NotificacaoJpa> findByDestinatarioIdOrderByCriadaEmDesc(int destinatarioId);
    Optional<NotificacaoJpa> findByDestinatarioIdAndTipoAndReferenciaId(int dest, String tipo, int refId);
    int countByDestinatarioIdAndLida(int destinatarioId, boolean lida);
    @Modifying
    @Query("UPDATE NotificacaoJpa n SET n.lida = true WHERE n.destinatarioId = :destId AND n.lida = false")
    void marcarTodasComoLidas(@Param("destId") int destinatarioId);
}

@Repository
class ItemFeedRepositorioImpl implements ItemFeedRepositorio {

    @Autowired ItemFeedJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public ItemFeed salvar(ItemFeed item) {
        var jpa  = mapeador.paraJpa(item);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<ItemFeed> obter(ItemFeedId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<ItemFeed> listarPorAutores(List<PerfilId> autoresIds) {
        if (autoresIds.isEmpty()) return List.of();
        var ids = autoresIds.stream().map(PerfilId::getId).toList();
        return repositorio.findByAutorIdIn(ids).stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

@Repository
class CurtidaRepositorioImpl implements CurtidaRepositorio {

    @Autowired CurtidaJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public Curtida salvar(Curtida curtida) {
        var jpa  = mapeador.paraJpa(curtida);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public void remover(CurtidaId id) {
        repositorio.deleteById(id.getId());
    }

    @Override
    public Optional<Curtida> obterPorPar(PerfilId usuarioId, ItemFeedId itemId) {
        return repositorio.findByUsuarioIdAndItemId(usuarioId.getId(), itemId.getId())
            .map(mapeador::paraDominio);
    }

    @Override
    public int contarPorItem(ItemFeedId itemId) {
        return repositorio.countByItemId(itemId.getId());
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

@Repository
class NotificacaoRepositorioImpl implements NotificacaoRepositorio {

    @Autowired NotificacaoJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public Notificacao salvar(Notificacao notificacao) {
        var jpa  = mapeador.paraJpa(notificacao);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public void remover(NotificacaoId id) {
        repositorio.deleteById(id.getId());
    }

    @Override
    public Optional<Notificacao> obter(NotificacaoId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public Optional<Notificacao> obterPorTipoERef(PerfilId destinatarioId, TipoNotificacao tipo, int referenciaId) {
        if (destinatarioId == null || tipo == null) return Optional.empty();
        return repositorio.findByDestinatarioIdAndTipoAndReferenciaId(
                destinatarioId.getId(), tipo.name(), referenciaId)
            .map(mapeador::paraDominio);
    }

    @Override
    public Optional<Notificacao> obterPorPedido(PedidoConexaoId pedidoId) {
        return repositorio.findAll().stream()
            .filter(n -> n.tipo.equals(TipoNotificacao.PEDIDO_CONEXAO.name())
                      && n.referenciaId == pedidoId.getId())
            .findFirst()
            .map(mapeador::paraDominio);
    }

    @Override
    public List<Notificacao> listarPorDestinatario(PerfilId destinatarioId) {
        return repositorio.findByDestinatarioIdOrderByCriadaEmDesc(destinatarioId.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public int contarNaoLidas(PerfilId destinatarioId) {
        return repositorio.countByDestinatarioIdAndLida(destinatarioId.getId(), false);
    }

    @Transactional
    @Override
    public void marcarTodasComoLidas(PerfilId destinatarioId) {
        repositorio.marcarTodasComoLidas(destinatarioId.getId());
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

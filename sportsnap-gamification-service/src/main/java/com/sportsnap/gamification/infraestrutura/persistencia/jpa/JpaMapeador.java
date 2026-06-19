package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sportsnap.gamification.dominio.atleta.Atleta;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.atleta.Email;
import com.sportsnap.gamification.dominio.bloqueio.Bloqueio;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.conexao.Conexao;
import com.sportsnap.gamification.dominio.conexao.ConexaoId;
import com.sportsnap.gamification.dominio.conexao.PedidoConexao;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoId;
import com.sportsnap.gamification.dominio.conexao.StatusPedido;
import com.sportsnap.gamification.dominio.feed.Curtida;
import com.sportsnap.gamification.dominio.feed.CurtidaId;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.notificacao.Notificacao;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoId;
import com.sportsnap.gamification.dominio.notificacao.TipoNotificacao;
import com.sportsnap.gamification.dominio.perfil.Perfil;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;

@Component
class JpaMapeador {

    Atleta paraDominio(AtletaJpa jpa) {
        return new Atleta(new AtletaId(jpa.id), jpa.nome, new Email(jpa.email));
    }

    AtletaJpa paraJpa(Atleta dominio) {
        var jpa = new AtletaJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.nome = dominio.getNome();
        jpa.email = dominio.getEmail().getEndereco();
        return jpa;
    }

    CartaOficial paraDominio(CartaOficialJpa jpa) {
        var atletaId = new AtletaId(jpa.atletaId);
        List<AtributoEsportivo> atributos = jpa.atributos.stream()
            .map(a -> new AtributoEsportivo(a.nome, a.valor, a.peso, a.tipoEsporte))
            .toList();
        return new CartaOficial(atletaId, atributos, jpa.overall, jpa.ultimaSincronizacao);
    }

    CartaOficialJpa paraJpa(CartaOficial dominio) {
        var jpa = new CartaOficialJpa();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.overall = dominio.getOverall();
        jpa.ultimaSincronizacao = dominio.getUltimaSincronizacao();
        jpa.atributos = dominio.getAtributos().stream()
            .map(a -> {
                var aJpa = new AtributoEsportivoJpa();
                aJpa.nome = a.getNome();
                aJpa.valor = a.getValor();
                aJpa.peso = a.getPeso();
                aJpa.tipoEsporte = a.getTipoEsporte();
                return aJpa;
            })
            .collect(Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    StatusPotencial paraDominio(StatusPotencialJpa jpa) {
        return new StatusPotencial(new AtletaId(jpa.atletaId),
            jpa.xpAcumulado, jpa.streakConsistencia, jpa.ultimaAtividade);
    }

    StatusPotencialJpa paraJpa(StatusPotencial dominio) {
        var jpa = new StatusPotencialJpa();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.xpAcumulado = dominio.getXpAcumulado();
        jpa.streakConsistencia = dominio.getStreakConsistencia();
        jpa.ultimaAtividade = dominio.getUltimaAtividade();
        return jpa;
    }

    Licenca paraDominio(LicencaJpa jpa) {
        return new Licenca(new AtletaId(jpa.atletaId), jpa.adquiridaEm);
    }

    LicencaJpa paraJpa(Licenca dominio) {
        var jpa = new LicencaJpa();
        jpa.atletaId    = dominio.getAtletaId().getId();
        jpa.adquiridaEm = dominio.getAdquiridaEm();
        return jpa;
    }

    // --- Perfil ---

    Perfil paraDominio(PerfilJpa jpa) {
        return new Perfil(new PerfilId(jpa.id), new AtletaId(jpa.usuarioId),
            jpa.nomeExibicao, TipoConta.valueOf(jpa.tipoConta), jpa.bio,
            jpa.esporte, jpa.localidade,
            jpa.visibilidade != null ? Visibilidade.valueOf(jpa.visibilidade) : Visibilidade.PUBLICA,
            jpa.totalSeguidores, jpa.totalSeguindo);
    }

    PerfilJpa paraJpa(Perfil dominio) {
        var jpa = new PerfilJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.usuarioId       = dominio.getUsuarioId().getId();
        jpa.nomeExibicao    = dominio.getNomeExibicao();
        jpa.tipoConta       = dominio.getTipoConta().name();
        jpa.bio             = dominio.getBio();
        jpa.esporte         = dominio.getEsporte();
        jpa.localidade      = dominio.getLocalidade();
        jpa.visibilidade    = dominio.getVisibilidade().name();
        jpa.totalSeguidores = dominio.getTotalSeguidores();
        jpa.totalSeguindo   = dominio.getTotalSeguindo();
        return jpa;
    }

    // --- Conexao ---

    Conexao paraDominio(ConexaoJpa jpa) {
        return new Conexao(new ConexaoId(jpa.id),
            new PerfilId(jpa.seguidorId), new PerfilId(jpa.seguidoId), jpa.criadaEm);
    }

    ConexaoJpa paraJpa(Conexao dominio) {
        var jpa = new ConexaoJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.seguidorId = dominio.getSeguidorId().getId();
        jpa.seguidoId  = dominio.getSeguidoId().getId();
        jpa.criadaEm   = dominio.getCriadaEm() != null ? dominio.getCriadaEm() : LocalDateTime.now();
        return jpa;
    }

    // --- PedidoConexao ---

    PedidoConexao paraDominio(PedidoConexaoJpa jpa) {
        return new PedidoConexao(new PedidoConexaoId(jpa.id),
            new PerfilId(jpa.solicitanteId), new PerfilId(jpa.alvoId),
            jpa.criadoEm, StatusPedido.valueOf(jpa.status));
    }

    PedidoConexaoJpa paraJpa(PedidoConexao dominio) {
        var jpa = new PedidoConexaoJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.solicitanteId = dominio.getSolicitanteId().getId();
        jpa.alvoId        = dominio.getAlvoId().getId();
        jpa.criadoEm      = dominio.getCriadoEm() != null ? dominio.getCriadoEm() : LocalDateTime.now();
        jpa.status        = dominio.getStatus().name();
        return jpa;
    }

    // --- Bloqueio ---

    Bloqueio paraDominio(BloqueioJpa jpa) {
        return new Bloqueio(new BloqueioId(jpa.id),
            new PerfilId(jpa.bloqueadorId), new PerfilId(jpa.bloqueadoId));
    }

    BloqueioJpa paraJpa(Bloqueio dominio) {
        var jpa = new BloqueioJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.bloqueadorId = dominio.getBloqueadorId().getId();
        jpa.bloqueadoId  = dominio.getBloqueadoId().getId();
        return jpa;
    }

    // --- ItemFeed ---

    ItemFeed paraDominio(ItemFeedJpa jpa) {
        return new ItemFeed(new ItemFeedId(jpa.id), new PerfilId(jpa.autorId),
            TipoItemFeed.valueOf(jpa.tipo), jpa.referenciaId, jpa.publicadoEm);
    }

    ItemFeedJpa paraJpa(ItemFeed dominio) {
        var jpa = new ItemFeedJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.autorId      = dominio.getAutorId().getId();
        jpa.tipo         = dominio.getTipo().name();
        jpa.referenciaId = dominio.getReferenciaId();
        jpa.publicadoEm  = dominio.getPublicadoEm() != null ? dominio.getPublicadoEm() : LocalDateTime.now();
        return jpa;
    }

    // --- Curtida ---

    Curtida paraDominio(CurtidaJpa jpa) {
        return new Curtida(new CurtidaId(jpa.id),
            new PerfilId(jpa.usuarioId), new ItemFeedId(jpa.itemId));
    }

    CurtidaJpa paraJpa(Curtida dominio) {
        var jpa = new CurtidaJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.usuarioId = dominio.getUsuarioId().getId();
        jpa.itemId    = dominio.getItemId().getId();
        return jpa;
    }

    // --- Notificacao ---

    Notificacao paraDominio(NotificacaoJpa jpa) {
        return new Notificacao(new NotificacaoId(jpa.id), new PerfilId(jpa.destinatarioId),
            TipoNotificacao.valueOf(jpa.tipo), jpa.referenciaId,
            jpa.lida, jpa.criadaEm, jpa.numAtores);
    }

    NotificacaoJpa paraJpa(Notificacao dominio) {
        var jpa = new NotificacaoJpa();
        if (dominio.getId() != null) jpa.id = dominio.getId().getId();
        jpa.destinatarioId = dominio.getDestinatarioId().getId();
        jpa.tipo           = dominio.getTipo().name();
        jpa.referenciaId   = dominio.getReferenciaId();
        jpa.lida           = dominio.isLida();
        jpa.criadaEm       = dominio.getCriadaEm() != null ? dominio.getCriadaEm() : LocalDateTime.now();
        jpa.numAtores      = dominio.getNumAtores();
        return jpa;
    }
}

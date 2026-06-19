package com.sportsnap.gamification;

import com.sportsnap.gamification.aplicacao.atleta.AtletaRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.atleta.AtletaServicoAplicacao;
import com.sportsnap.gamification.aplicacao.perfil.PerfilRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.perfil.PerfilServicoAplicacao;
import com.sportsnap.gamification.aplicacao.ranking.RankingRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.ranking.RankingServicoAplicacao;
import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.atleta.AtletaServico;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.feed.CurtidaRepositorio;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeedRepositorio;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoRepositorio;
import com.sportsnap.gamification.dominio.notificacao.NotificacaoServico;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilServico;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.ranking.RankingProxi;
import com.sportsnap.gamification.dominio.ranking.RankingServico;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico;
import com.sportsnap.gamification.dominio.xp.EstrategiaXpCorrida;
import com.sportsnap.gamification.dominio.xp.EstrategiaXpMusculacao;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GamificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamificationApplication.class, args);
    }

    @Bean
    public AtletaServico atletaServico(AtletaRepositorio repositorio) {
        return new AtletaServico(repositorio);
    }

    @Bean
    public AtletaServicoAplicacao atletaServicoAplicacao(AtletaRepositorioAplicacao repositorio) {
        return new AtletaServicoAplicacao(repositorio);
    }

    @Bean
    public SincronizacaoServico sincronizacaoServico(CartaOficialRepositorio cartaRepositorio,
                                                      StatusPotencialRepositorio statusRepositorio,
                                                      LicencaRepositorio licencaRepositorio,
                                                      EventoBarramento barramento) {
        return new SincronizacaoServico(cartaRepositorio, statusRepositorio, licencaRepositorio, barramento);
    }

    @Bean
    public RankingServico rankingServico(CartaOficialRepositorio cartaRepositorio) {
        return new RankingServico(cartaRepositorio);
    }

    @Bean
    public RankingProxi rankingProxi(RankingServico rankingServico) {
        return new RankingProxi(rankingServico);
    }

    @Bean
    public RankingServicoAplicacao rankingServicoAplicacao(RankingRepositorioAplicacao repositorio) {
        return new RankingServicoAplicacao(repositorio);
    }

    @Bean
    public EstrategiaXpCorrida estrategiaXpCorrida() {
        return new EstrategiaXpCorrida();
    }

    @Bean
    public EstrategiaXpMusculacao estrategiaXpMusculacao() {
        return new EstrategiaXpMusculacao();
    }

    // --- F1: Perfil Social e Rede de Conexoes ---

    @Bean
    public PerfilServico perfilServico(PerfilRepositorio repositorio) {
        return new PerfilServico(repositorio);
    }

    @Bean
    public PerfilServicoAplicacao perfilServicoAplicacao(PerfilRepositorioAplicacao repositorio) {
        return new PerfilServicoAplicacao(repositorio);
    }

    @Bean
    public ConexaoServico conexaoServico(PerfilRepositorio perfilRepositorio,
                                          ConexaoRepositorio conexaoRepositorio,
                                          PedidoConexaoRepositorio pedidoRepositorio,
                                          BloqueioRepositorio bloqueioRepositorio,
                                          EventoBarramento barramento) {
        return new ConexaoServico(perfilRepositorio, conexaoRepositorio,
            pedidoRepositorio, bloqueioRepositorio, barramento);
    }

    // --- F2: Feed de Atividades e Notificacoes ---

    @Bean
    public FeedServico feedServico(ItemFeedRepositorio itemFeedRepositorio,
                                    CurtidaRepositorio curtidaRepositorio,
                                    ConexaoRepositorio conexaoRepositorio,
                                    BloqueioRepositorio bloqueioRepositorio,
                                    EventoBarramento barramento) {
        return new FeedServico(itemFeedRepositorio, curtidaRepositorio,
            conexaoRepositorio, bloqueioRepositorio, barramento);
    }

    @Bean
    public NotificacaoServico notificacaoServico(NotificacaoRepositorio repositorio) {
        return new NotificacaoServico(repositorio);
    }
}

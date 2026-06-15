package com.sportsnap.gamification;

import com.sportsnap.gamification.aplicacao.atleta.AtletaRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.atleta.AtletaServicoAplicacao;
import com.sportsnap.gamification.aplicacao.ranking.RankingRepositorioAplicacao;
import com.sportsnap.gamification.aplicacao.ranking.RankingServicoAplicacao;
import com.sportsnap.gamification.dominio.analise.AnaliseServico;
import com.sportsnap.gamification.dominio.analise.DistanciaEuclidiana;
import com.sportsnap.gamification.dominio.analise.MetricaSimilaridade;
import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.atleta.AtletaServico;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.competicao.CalculoEloEstrategia;
import com.sportsnap.gamification.dominio.competicao.CompeticaoServico;
import com.sportsnap.gamification.dominio.competicao.ConfrontoRepositorio;
import com.sportsnap.gamification.dominio.competicao.EloPadrao;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRankingRepositorio;
import com.sportsnap.gamification.dominio.competicao.TemporadaRepositorio;
import com.sportsnap.gamification.dominio.competicao.TemporadaServico;
import com.sportsnap.gamification.dominio.desafio.DesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.DesafioServico;
import com.sportsnap.gamification.dominio.desafio.InsigniaRepositorio;
import com.sportsnap.gamification.dominio.desafio.MotorDesafios;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafioRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.ranking.RankingProxi;
import com.sportsnap.gamification.dominio.ranking.RankingServico;
import com.sportsnap.gamification.dominio.reveal.CustoEvolucaoEstrategia;
import com.sportsnap.gamification.dominio.reveal.CustoProgressivo;
import com.sportsnap.gamification.dominio.reveal.MotorAlocacao;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacaoRepositorio;
import com.sportsnap.gamification.dominio.reveal.RevealServico;
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

    @Bean
    public CustoEvolucaoEstrategia custoEvolucaoEstrategia() {
        return new CustoProgressivo();
    }

    @Bean
    public MotorAlocacao motorAlocacao(CustoEvolucaoEstrategia custoEstrategia) {
        return new MotorAlocacao(custoEstrategia);
    }

    @Bean
    public RevealServico revealServico(CartaOficialRepositorio cartaRepositorio,
                                       StatusPotencialRepositorio statusRepositorio,
                                       LicencaRepositorio licencaRepositorio,
                                       RegistroSincronizacaoRepositorio sincronizacaoRepositorio,
                                       RegistroEvolucaoRepositorio evolucaoRepositorio,
                                       MotorAlocacao motorAlocacao,
                                       EventoBarramento barramento) {
        return new RevealServico(cartaRepositorio, statusRepositorio, licencaRepositorio,
            sincronizacaoRepositorio, evolucaoRepositorio, motorAlocacao, barramento);
    }

    @Bean
    public CalculoEloEstrategia calculoEloEstrategia() {
        return new EloPadrao();
    }

    @Bean
    public CompeticaoServico competicaoServico(CartaOficialRepositorio cartaRepositorio,
                                               PontuacaoRankingRepositorio pontuacaoRepositorio,
                                               ConfrontoRepositorio confrontoRepositorio,
                                               TemporadaRepositorio temporadaRepositorio,
                                               RegistroSincronizacaoRepositorio sincronizacaoRepositorio,
                                               CalculoEloEstrategia elo) {
        return new CompeticaoServico(cartaRepositorio, pontuacaoRepositorio, confrontoRepositorio,
            temporadaRepositorio, sincronizacaoRepositorio, elo);
    }

    @Bean
    public TemporadaServico temporadaServico(TemporadaRepositorio temporadaRepositorio,
                                             PontuacaoRankingRepositorio pontuacaoRepositorio) {
        return new TemporadaServico(temporadaRepositorio, pontuacaoRepositorio);
    }

    @Bean
    public MetricaSimilaridade metricaSimilaridade() {
        return new DistanciaEuclidiana();
    }

    @Bean
    public AnaliseServico analiseServico(CartaOficialRepositorio cartaRepositorio,
                                         RegistroEvolucaoRepositorio evolucaoRepositorio,
                                         MetricaSimilaridade metrica) {
        return new AnaliseServico(cartaRepositorio, evolucaoRepositorio, metrica);
    }

    @Bean
    public DesafioServico desafioServico(DesafioRepositorio desafioRepositorio,
                                         ProgressoDesafioRepositorio progressoRepositorio,
                                         AnaliseServico analiseServico) {
        return new DesafioServico(desafioRepositorio, progressoRepositorio, analiseServico);
    }

    @Bean
    public MotorDesafios motorDesafios(DesafioRepositorio desafioRepositorio,
                                       ProgressoDesafioRepositorio progressoRepositorio,
                                       InsigniaRepositorio insigniaRepositorio,
                                       EventoBarramento barramento) {
        return new MotorDesafios(desafioRepositorio, progressoRepositorio, insigniaRepositorio, barramento);
    }
}

package com.sportsnap.gamification;

import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.atleta.AtletaServico;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.ranking.RankingServico;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class GamificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamificationApplication.class, args);
    }

    @Bean
    public AtletaServico atletaServico(AtletaRepositorio repositorio) {
        return new AtletaServico(repositorio);
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
}

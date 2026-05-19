package com.sportsnap.session;

import com.sportsnap.session.aplicacao.sessao.SessaoRepositorioAplicacao;
import com.sportsnap.session.aplicacao.sessao.SessaoServicoAplicacao;
import com.sportsnap.session.aplicacao.spot.SpotRepositorioAplicacao;
import com.sportsnap.session.aplicacao.spot.SpotServicoAplicacao;
import com.sportsnap.session.aplicacao.checkin.CheckInLoteServicoAplicacao;
import com.sportsnap.session.dominio.atividade.AtividadeServico;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeRepositorio;
import com.sportsnap.session.dominio.checkin.CheckInRepositorio;
import com.sportsnap.session.dominio.checkin.CheckInServico;
import com.sportsnap.session.dominio.evento.EventoBarramento;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.SpotRepositorio;
import com.sportsnap.session.dominio.spot.SpotServico;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class SessionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SessionServiceApplication.class, args);
    }

    @Bean
    public SimpleApplicationEventMulticaster applicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }

    @Bean
    public SpotServico spotServico(SpotRepositorio repositorio) {
        return new SpotServico(repositorio);
    }

    @Bean
    public SpotServicoAplicacao spotServicoAplicacao(SpotRepositorioAplicacao repositorio) {
        return new SpotServicoAplicacao(repositorio);
    }

    @Bean
    public SessaoServico sessaoServico(SessaoRepositorio repositorio, SpotRepositorio spotRepositorio) {
        return new SessaoServico(repositorio, spotRepositorio);
    }

    @Bean
    public SessaoServicoAplicacao sessaoServicoAplicacao(SessaoRepositorioAplicacao repositorio) {
        return new SessaoServicoAplicacao(repositorio);
    }

    @Bean
    public CheckInServico checkInServico(CheckInRepositorio repositorio,
                                          SessaoRepositorio sessaoRepositorio,
                                          EventoBarramento barramento) {
        return new CheckInServico(repositorio, sessaoRepositorio, barramento);
    }

    @Bean
    public AtividadeServico atividadeServico(RegistroAtividadeRepositorio repositorio,
                                              CheckInRepositorio checkInRepositorio,
                                              EventoBarramento barramento) {
        return new AtividadeServico(repositorio, checkInRepositorio, barramento);
    }

    @Bean
    public ExecutorService checkInExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public CheckInLoteServicoAplicacao checkInLoteServicoAplicacao(CheckInServico checkInServico,
                                                                    ExecutorService checkInExecutor) {
        return new CheckInLoteServicoAplicacao(checkInServico, checkInExecutor);
    }
}

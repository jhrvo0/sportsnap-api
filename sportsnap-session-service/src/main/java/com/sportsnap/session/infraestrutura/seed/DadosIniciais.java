package com.sportsnap.session.infraestrutura.seed;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotRepositorio;

@Component
@Order(1)
@ConditionalOnProperty(name = "sportsnap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private final SpotRepositorio spotRepositorio;
    private final SessaoRepositorio sessaoRepositorio;

    public DadosIniciais(SpotRepositorio spotRepositorio, SessaoRepositorio sessaoRepositorio) {
        this.spotRepositorio = spotRepositorio;
        this.sessaoRepositorio = sessaoRepositorio;
    }

    @Override
    public void run(String... args) {
        if (!spotRepositorio.listarTodos().isEmpty()) return;

        var maracaipe = spotRepositorio.salvar(new Spot(
            "Praia de Maracaipe",
            new Coordenada(-8.5341, -35.0070),
            "Spot de surf na Praia de Maracaipe, Pernambuco"));

        var ibirapuera = spotRepositorio.salvar(new Spot(
            "Parque Ibirapuera",
            new Coordenada(-23.5874, -46.6576),
            "Pista de corrida do Parque Ibirapuera, Sao Paulo"));

        var rezende = spotRepositorio.salvar(new Spot(
            "Pista de Skate do Rezende",
            new Coordenada(-8.0476, -34.8770),
            "Pista de skate em Recife"));

        spotRepositorio.salvar(new Spot(
            "Campo do Retiro",
            new Coordenada(-23.5505, -46.6333),
            "Campo society com gramado sintético. Ideal para rachões e treinos táticos."));

        spotRepositorio.salvar(new Spot(
            "Ciclovia da Orla",
            new Coordenada(-22.9110, -43.1726),
            "Ciclovia extensa ao longo da orla carioca. Ótima para pedais longos."));

        var agora = LocalDateTime.now();

        sessaoRepositorio.salvar(new Sessao(
            maracaipe.getId(),
            new Periodo(agora.minusHours(3), agora.plusHours(3)),
            "Surf matinal das 06h"));

        sessaoRepositorio.salvar(new Sessao(
            ibirapuera.getId(),
            new Periodo(agora.minusHours(1), agora.plusHours(2)),
            "Treino de corrida vespertino"));

        sessaoRepositorio.salvar(new Sessao(
            rezende.getId(),
            new Periodo(agora.plusHours(2), agora.plusHours(6)),
            "Sessao de skate noturna"));
    }
}

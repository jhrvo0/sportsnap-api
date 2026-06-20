package com.sportsnap.session.infraestrutura.seed;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.session.dominio.atividade.AtividadeServico;
import com.sportsnap.session.dominio.atividade.Intensidade;
import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInServico;
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
    private final CheckInServico checkInServico;
    private final AtividadeServico atividadeServico;

    public DadosIniciais(SpotRepositorio spotRepositorio,
                         SessaoRepositorio sessaoRepositorio,
                         CheckInServico checkInServico,
                         AtividadeServico atividadeServico) {
        this.spotRepositorio  = spotRepositorio;
        this.sessaoRepositorio = sessaoRepositorio;
        this.checkInServico   = checkInServico;
        this.atividadeServico = atividadeServico;
    }

    @Override
    public void run(String... args) {
        if (!spotRepositorio.listarTodos().isEmpty()) return;

        var agora = LocalDateTime.now();

        // ── Spots ─────────────────────────────────────────────────────────────
        var maracaipe  = spotRepositorio.salvar(new Spot("Praia de Maracaipe",
            new Coordenada(-8.5341, -35.0070),
            "Spot de surf em Pernambuco. Ondas constantes e ambiente familiar."));

        var ibirapuera = spotRepositorio.salvar(new Spot("Parque Ibirapuera",
            new Coordenada(-23.5874, -46.6576),
            "Pista de corrida de 3.6km no Parque Ibirapuera, São Paulo."));

        var rezende    = spotRepositorio.salvar(new Spot("Pista de Skate do Rezende",
            new Coordenada(-8.0476, -34.8770),
            "Pista pública de skate em Recife com bowl, corrimão e meia-pipa."));

        var campo      = spotRepositorio.salvar(new Spot("Campo do Retiro",
            new Coordenada(-23.5505, -46.6333),
            "Campo society com gramado sintético. Ideal para rachões e treinos táticos."));

        var ciclovia   = spotRepositorio.salvar(new Spot("Ciclovia da Orla",
            new Coordenada(-22.9110, -43.1726),
            "Ciclovia de 22km ao longo da orla carioca. Ótima para pedais e corridas."));

        // ── Sessões ───────────────────────────────────────────────────────────
        var sessaoSurf = sessaoRepositorio.salvar(new Sessao(
            maracaipe.getId(),
            new Periodo(agora.minusHours(4), agora.plusHours(4)),
            "Surf matinal — ondas de 1.5m, vento offshore"));

        var sessaoCorrida = sessaoRepositorio.salvar(new Sessao(
            ibirapuera.getId(),
            new Periodo(agora.minusHours(3), agora.plusHours(3)),
            "Treino de corrida vespertino — intervalados 400m"));

        var sessaoSkate = sessaoRepositorio.salvar(new Sessao(
            rezende.getId(),
            new Periodo(agora.minusHours(2), agora.plusHours(4)),
            "Sessão de skate — treino de tricks e sequências"));

        sessaoRepositorio.salvar(new Sessao(
            campo.getId(),
            new Periodo(agora.plusDays(1), agora.plusDays(1).plusHours(2)),
            "Jogo-treino — rachão tático semanal"));

        sessaoRepositorio.salvar(new Sessao(
            ciclovia.getId(),
            new Periodo(agora.plusDays(2).withHour(6), agora.plusDays(2).withHour(9)),
            "Pedal matinal — 40km pela orla"));

        // ── Check-ins ─────────────────────────────────────────────────────────
        // atletaId 1 = Maria (corrida)
        var ciMaria1 = checkInServico.realizar(
            new AtletaId(1), sessaoCorrida.getId(),
            new Coordenada(-23.5874, -46.6576));

        var ciMaria2 = checkInServico.realizar(
            new AtletaId(1), sessaoSurf.getId(),
            new Coordenada(-8.5341, -35.0070));

        // atletaId 2 = João (surf)
        var ciJoao1 = checkInServico.realizar(
            new AtletaId(2), sessaoSurf.getId(),
            new Coordenada(-8.5341, -35.0070));

        // atletaId 3 = Ana (skate)
        var ciAna1 = checkInServico.realizar(
            new AtletaId(3), sessaoSkate.getId(),
            new Coordenada(-8.0476, -34.8770));

        // atletaId 4 = Lucas (futebol)
        var ciLucas1 = checkInServico.realizar(
            new AtletaId(4), sessaoCorrida.getId(),
            new Coordenada(-23.5874, -46.6576));

        // atletaId 5 = Beatriz (ultra corrida)
        var ciBeatriz1 = checkInServico.realizar(
            new AtletaId(5), sessaoCorrida.getId(),
            new Coordenada(-23.5874, -46.6576));

        // ── Atividades ────────────────────────────────────────────────────────
        // Maria — corrida longa
        atividadeServico.registrarComCheckIn(
            new AtletaId(1), ciMaria1.getId(), "CORRIDA",
            agora.minusHours(2), 18.5, 5400L,
            Intensidade.ALTA, 90.0, 7, "Ritmo forte nos últimos 5km", "CHECKIN", null);

        // Maria — treino no mar (surf → corrida na areia)
        atividadeServico.registrarComCheckIn(
            new AtletaId(1), ciMaria2.getId(), "CORRIDA",
            agora.minusHours(1), 5.0, 1800L,
            Intensidade.MEDIA, 40.0, 5, "Corrida na areia antes do surf", "CHECKIN", null);

        // João — treino de surf
        atividadeServico.registrarComCheckIn(
            new AtletaId(2), ciJoao1.getId(), "SURF",
            agora.minusHours(2), 0.0, 7200L,
            Intensidade.ALTA, 80.0, 8, "Swell excelente, 15 ondas surfadas", "CHECKIN", null);

        // Ana — sessão de skate
        atividadeServico.registrarComCheckIn(
            new AtletaId(3), ciAna1.getId(), "SKATE",
            agora.plusHours(1), 0.0, 5400L,
            Intensidade.ALTA, 70.0, 8, "Heelflip switch landed!", "CHECKIN", null);

        // Lucas — treino físico (corrida)
        atividadeServico.registrarComCheckIn(
            new AtletaId(4), ciLucas1.getId(), "CORRIDA",
            agora.minusHours(1), 8.0, 2880L,
            Intensidade.MEDIA, 55.0, 6, "Intervalados 4x400m", "CHECKIN", null);

        // Beatriz — treino de trilha
        atividadeServico.registrarComCheckIn(
            new AtletaId(5), ciBeatriz1.getId(), "CORRIDA",
            agora.minusHours(3), 32.0, 14400L,
            Intensidade.ALTA, 160.0, 9, "Long run com 800m de desnível", "CHECKIN", null);
    }
}

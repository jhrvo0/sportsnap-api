package com.sportsnap.gamification.infraestrutura.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.gamification.dominio.atleta.Atleta;
import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.atleta.Email;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.competicao.CompeticaoServico;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRanking;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRankingRepositorio;
import com.sportsnap.gamification.dominio.competicao.TemporadaServico;
import com.sportsnap.gamification.dominio.desafio.Cadencia;
import com.sportsnap.gamification.dominio.desafio.CriterioDesafio;
import com.sportsnap.gamification.dominio.desafio.Desafio;
import com.sportsnap.gamification.dominio.desafio.DesafioServico;
import com.sportsnap.gamification.dominio.desafio.TipoCriterio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;

@Component
@Order(1)
@ConditionalOnProperty(name = "sportsnap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private final AtletaRepositorio atletaRepositorio;
    private final CartaOficialRepositorio cartaRepositorio;
    private final StatusPotencialRepositorio statusRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final TemporadaServico temporadaServico;
    private final PontuacaoRankingRepositorio pontuacaoRepositorio;
    private final DesafioServico desafioServico;

    public DadosIniciais(AtletaRepositorio atletaRepositorio,
                          CartaOficialRepositorio cartaRepositorio,
                          StatusPotencialRepositorio statusRepositorio,
                          LicencaRepositorio licencaRepositorio,
                          TemporadaServico temporadaServico,
                          PontuacaoRankingRepositorio pontuacaoRepositorio,
                          DesafioServico desafioServico) {
        this.atletaRepositorio = atletaRepositorio;
        this.cartaRepositorio = cartaRepositorio;
        this.statusRepositorio = statusRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.temporadaServico = temporadaServico;
        this.pontuacaoRepositorio = pontuacaoRepositorio;
        this.desafioServico = desafioServico;
    }

    @Override
    public void run(String... args) {
        if (!atletaRepositorio.listarTodos().isEmpty()) return;

        var maria = atletaRepositorio.salvar(new Atleta("Maria Atleta", new Email("maria@email.com")));
        var joao  = atletaRepositorio.salvar(new Atleta("Joao Silva",   new Email("joao@email.com")));
        var ana   = atletaRepositorio.salvar(new Atleta("Ana Costa",    new Email("ana@email.com")));

        cartaRepositorio.salvar(new CartaOficial(maria.getId(), List.of(
            new AtributoEsportivo("Velocidade",  78, 1.0, "CORRIDA"),
            new AtributoEsportivo("Resistencia", 82, 1.0, "CORRIDA"),
            new AtributoEsportivo("Tecnica",     74, 1.0, "CORRIDA")
        ), 78.0, LocalDateTime.now().minusDays(2)));

        cartaRepositorio.salvar(new CartaOficial(joao.getId(), List.of(
            new AtributoEsportivo("Forca",     85, 1.0, "MUSCULACAO"),
            new AtributoEsportivo("Explosao",  80, 1.0, "MUSCULACAO"),
            new AtributoEsportivo("Tecnica",   72, 1.0, "MUSCULACAO")
        ), 79.0, LocalDateTime.now().minusDays(5)));

        cartaRepositorio.salvar(new CartaOficial(ana.getId(), List.of(
            new AtributoEsportivo("Equilibrio", 88, 1.0, "SURF"),
            new AtributoEsportivo("Agilidade",  76, 1.0, "SURF"),
            new AtributoEsportivo("Tecnica",    70, 1.0, "SURF")
        )));

        statusRepositorio.salvar(new StatusPotencial(maria.getId(), 35.0, 4, LocalDateTime.now().minusHours(6)));
        statusRepositorio.salvar(new StatusPotencial(joao.getId(),  20.0, 2, LocalDateTime.now().minusHours(20)));
        statusRepositorio.salvar(new StatusPotencial(ana.getId(),   12.0, 1, LocalDateTime.now().minusHours(36)));

        licencaRepositorio.registrar(new Licenca(maria.getId(), LocalDateTime.now().minusHours(1)));
        licencaRepositorio.registrar(new Licenca(joao.getId(),  LocalDateTime.now().minusDays(1)));

        // Temporada vigente e pontuacao de ranking inicial para a competicao (F1)
        temporadaServico.criar("CORRIDA", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        pontuacaoRepositorio.salvar(new PontuacaoRanking(maria.getId(), CompeticaoServico.PR_INICIAL));
        pontuacaoRepositorio.salvar(new PontuacaoRanking(joao.getId(),  CompeticaoServico.PR_INICIAL));

        // Base adicional de corredores sincronizados para habilitar a Analise (F2: percentil/similaridade)
        int[][] corredores = {
            // velocidade, resistencia, tecnica
            {72, 75, 68}, {84, 70, 80}, {66, 88, 71}, {90, 79, 85}
        };
        String[] nomes = {"Pedro Lima", "Lucas Reis", "Bruno Alves", "Rafa Souza"};
        for (int i = 0; i < nomes.length; i++) {
            var atleta = atletaRepositorio.salvar(new Atleta(nomes[i],
                new Email("corredor" + (i + 1) + "@email.com")));
            int[] v = corredores[i];
            double overall = (v[0] + v[1] + v[2]) / 3.0;
            cartaRepositorio.salvar(new CartaOficial(atleta.getId(), List.of(
                new AtributoEsportivo("Velocidade",  v[0], 1.0, "CORRIDA"),
                new AtributoEsportivo("Resistencia", v[1], 1.0, "CORRIDA"),
                new AtributoEsportivo("Tecnica",     v[2], 1.0, "CORRIDA")
            ), overall, LocalDateTime.now().minusDays(3)));
            pontuacaoRepositorio.salvar(new PontuacaoRanking(atleta.getId(), CompeticaoServico.PR_INICIAL));
        }

        // Desafios iniciais para o Motor de Desafios (F2)
        desafioServico.definir(new Desafio(
            "Treino Firme",
            List.of(CriterioDesafio.de(TipoCriterio.CONTAGEM_SINCRONIZACOES, 2)),
            null, null, true, "ESFORCO", List.of(), Cadencia.NENHUMA, false));
        desafioServico.definir(new Desafio(
            "Overall de Elite",
            List.of(CriterioDesafio.de(TipoCriterio.LIMIAR_OVERALL, 85)),
            null, null, true, "ELITE", List.of(), Cadencia.NENHUMA, false));
        // Desafios direcionados a atributos (alimentam a sugestao de ponto fraco)
        desafioServico.definir(new Desafio(
            "Acelere: Velocidade 85",
            List.of(new CriterioDesafio(TipoCriterio.LIMIAR_ATRIBUTO, 85, "Velocidade")),
            null, null, true, "VELOZ", List.of(), Cadencia.NENHUMA, false));
        desafioServico.definir(new Desafio(
            "Fôlego: Resistência 85",
            List.of(new CriterioDesafio(TipoCriterio.LIMIAR_ATRIBUTO, 85, "Resistencia")),
            null, null, true, "RESILIENTE", List.of(), Cadencia.NENHUMA, false));
        desafioServico.definir(new Desafio(
            "Refino: Técnica 85",
            List.of(new CriterioDesafio(TipoCriterio.LIMIAR_ATRIBUTO, 85, "Tecnica")),
            null, null, true, "TECNICO", List.of(), Cadencia.NENHUMA, false));
    }
}

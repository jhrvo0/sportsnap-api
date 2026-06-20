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
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.perfil.Perfil;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilServico;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;
import com.sportsnap.gamification.dominio.post.PostEsportivoServico;
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
    private final PerfilRepositorio perfilRepositorio;
    private final PerfilServico perfilServico;
    private final ConexaoServico conexaoServico;
    private final PostEsportivoServico postServico;
    private final FeedServico feedServico;

    public DadosIniciais(AtletaRepositorio atletaRepositorio,
                         CartaOficialRepositorio cartaRepositorio,
                         StatusPotencialRepositorio statusRepositorio,
                         LicencaRepositorio licencaRepositorio,
                         PerfilRepositorio perfilRepositorio,
                         PerfilServico perfilServico,
                         ConexaoServico conexaoServico,
                         PostEsportivoServico postServico,
                         FeedServico feedServico) {
        this.atletaRepositorio  = atletaRepositorio;
        this.cartaRepositorio   = cartaRepositorio;
        this.statusRepositorio  = statusRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.perfilRepositorio  = perfilRepositorio;
        this.perfilServico      = perfilServico;
        this.conexaoServico     = conexaoServico;
        this.postServico        = postServico;
        this.feedServico        = feedServico;
    }

    @Override
    public void run(String... args) {
        if (!atletaRepositorio.listarTodos().isEmpty()) return;

        // ── Atletas ──────────────────────────────────────────────────────────────
        var maria   = atletaRepositorio.salvar(new Atleta("Maria Silva",    new Email("maria@sportsnap.com")));
        var joao    = atletaRepositorio.salvar(new Atleta("Joao Costa",     new Email("joao@sportsnap.com")));
        var ana     = atletaRepositorio.salvar(new Atleta("Ana Ferreira",   new Email("ana@sportsnap.com")));
        var lucas   = atletaRepositorio.salvar(new Atleta("Lucas Oliveira", new Email("lucas@sportsnap.com")));
        var beatriz = atletaRepositorio.salvar(new Atleta("Beatriz Santos", new Email("beatriz@sportsnap.com")));

        // ── Cartas Oficiais (compatibilidade com ranking) ─────────────────────
        cartaRepositorio.salvar(new CartaOficial(maria.getId(), List.of(
            new AtributoEsportivo("Velocidade",  82, 1.0, "CORRIDA"),
            new AtributoEsportivo("Resistencia", 87, 1.5, "CORRIDA"),
            new AtributoEsportivo("Tecnica",     79, 1.0, "CORRIDA")
        ), 83.2, LocalDateTime.now().minusDays(3)));

        cartaRepositorio.salvar(new CartaOficial(joao.getId(), List.of(
            new AtributoEsportivo("Equilibrio",  90, 1.5, "SURF"),
            new AtributoEsportivo("Agilidade",   85, 1.0, "SURF"),
            new AtributoEsportivo("Tecnica",     88, 1.0, "SURF")
        ), 87.8, LocalDateTime.now().minusDays(1)));

        cartaRepositorio.salvar(new CartaOficial(ana.getId(), List.of(
            new AtributoEsportivo("Criatividade", 91, 1.0, "SKATE"),
            new AtributoEsportivo("Tecnica",      86, 1.5, "SKATE"),
            new AtributoEsportivo("Consistencia", 80, 1.0, "SKATE")
        ), 86.0, LocalDateTime.now().minusDays(7)));

        cartaRepositorio.salvar(new CartaOficial(lucas.getId(), List.of(
            new AtributoEsportivo("Forca",        84, 1.0, "FUTEBOL"),
            new AtributoEsportivo("Posicionamento",88, 1.5, "FUTEBOL"),
            new AtributoEsportivo("Passe",        76, 1.0, "FUTEBOL")
        ), 83.6, LocalDateTime.now().minusDays(5)));

        cartaRepositorio.salvar(new CartaOficial(beatriz.getId(), List.of(
            new AtributoEsportivo("Resistencia",  95, 2.0, "CORRIDA"),
            new AtributoEsportivo("Cadencia",     88, 1.0, "CORRIDA"),
            new AtributoEsportivo("Tecnica",      82, 1.0, "CORRIDA")
        ), 90.0, LocalDateTime.now().minusDays(2)));

        // ── Shadow Stats ─────────────────────────────────────────────────────
        statusRepositorio.salvar(new StatusPotencial(maria.getId(),   42.0, 6, LocalDateTime.now().minusHours(4)));
        statusRepositorio.salvar(new StatusPotencial(joao.getId(),    28.5, 4, LocalDateTime.now().minusHours(8)));
        statusRepositorio.salvar(new StatusPotencial(ana.getId(),     15.0, 2, LocalDateTime.now().minusDays(1)));
        statusRepositorio.salvar(new StatusPotencial(lucas.getId(),   33.0, 5, LocalDateTime.now().minusHours(12)));
        statusRepositorio.salvar(new StatusPotencial(beatriz.getId(), 60.0, 9, LocalDateTime.now().minusHours(2)));

        // ── Licenças Gamification (para elegibilidade de sincronização) ──────
        licencaRepositorio.registrar(new Licenca(maria.getId(),   LocalDateTime.now().minusHours(1)));
        licencaRepositorio.registrar(new Licenca(joao.getId(),    LocalDateTime.now().minusDays(1)));
        licencaRepositorio.registrar(new Licenca(lucas.getId(),   LocalDateTime.now().minusHours(6)));
        licencaRepositorio.registrar(new Licenca(beatriz.getId(), LocalDateTime.now().minusHours(3)));

        // ── Perfis Sociais ───────────────────────────────────────────────────
        var pMaria   = perfilServico.criar(maria.getId(),   "Maria Silva",    TipoConta.ATLETA);
        var pJoao    = perfilServico.criar(joao.getId(),    "João Costa",     TipoConta.ATLETA);
        var pAna     = perfilServico.criar(ana.getId(),     "Ana Ferreira",   TipoConta.ATLETA);
        var pLucas   = perfilServico.criar(lucas.getId(),   "Lucas Oliveira", TipoConta.ATLETA);
        var pBeatriz = perfilServico.criar(beatriz.getId(), "Beatriz Santos", TipoConta.ATLETA);

        perfilServico.editar(pMaria.getId(), maria.getId(),
            "Maria Silva",
            "Maratonista apaixonada. 3x top-10 em São Paulo. Treino às 5h da manhã todo dia 🏃",
            "Corrida", "São Paulo, SP", Visibilidade.PUBLICA, null);

        perfilServico.editar(pJoao.getId(), joao.getId(),
            "João Costa",
            "Surfista desde os 10 anos. Cresci em Maracaipe e nunca mais larguei a prancha 🏄",
            "Surf", "Recife, PE", Visibilidade.PUBLICA, null);

        perfilServico.editar(pAna.getId(), ana.getId(),
            "Ana Ferreira",
            "Skatista profissional. Street e park. Representando Recife no circuito nacional 🛹",
            "Skate", "Recife, PE", Visibilidade.PUBLICA, null);

        perfilServico.editar(pLucas.getId(), lucas.getId(),
            "Lucas Oliveira",
            "Zagueiro. Futebol é tática e disciplina. Treino técnico e físico diariamente ⚽",
            "Futebol", "São Paulo, SP", Visibilidade.PUBLICA, null);

        perfilServico.editar(pBeatriz.getId(), beatriz.getId(),
            "Beatriz Santos",
            "Ultra runner. Montanhas e trilhas são meu habitat. 100km já foram 🏔️",
            "Corrida", "Curitiba, PR", Visibilidade.PRIVADA, null);

        // ── Conexões (seguimento) ─────────────────────────────────────────────
        // Maria segue João e Ana
        conexaoServico.seguir(pMaria.getId(), pJoao.getId());
        conexaoServico.seguir(pMaria.getId(), pAna.getId());
        // João segue Maria e Lucas
        conexaoServico.seguir(pJoao.getId(), pMaria.getId());
        conexaoServico.seguir(pJoao.getId(), pLucas.getId());
        // Ana segue Maria, João e Lucas
        conexaoServico.seguir(pAna.getId(), pMaria.getId());
        conexaoServico.seguir(pAna.getId(), pJoao.getId());
        conexaoServico.seguir(pAna.getId(), pLucas.getId());
        // Lucas segue João e Ana
        conexaoServico.seguir(pLucas.getId(), pJoao.getId());
        conexaoServico.seguir(pLucas.getId(), pAna.getId());
        // Beatriz tem conta privada — Maria e Ana enviam pedido pendente
        conexaoServico.seguir(pMaria.getId(), pBeatriz.getId());
        conexaoServico.seguir(pAna.getId(),   pBeatriz.getId());

        // ── Posts Esportivos ─────────────────────────────────────────────────
        postServico.criar(pMaria.getId(),
            "Finalizei meu primeiro treino de 25km essa semana! 🎉 A resistência está melhorando muito depois que ajustei minha cadência de passada. Menos de 5min/km nos últimos 10km. Próxima meta: São Silvestre 2026!",
            "Corrida");

        postServico.criar(pJoao.getId(),
            "As ondas de Maracaipe estão perfeitas essa semana. Swell de 1.5m, vento offshore e sol o dia todo ☀️ Se você surfa e está em Recife, não perde! Estarei lá amanhã de manhã cedo.",
            "Surf");

        postServico.criar(pAna.getId(),
            "Aprendi o heelflip switch hoje depois de 3 horas de tentativa 😅 O skate ensina que persistência > talento. Cada queda é um passo pra frente. Quem mais treina na Pista do Rezende?",
            "Skate");

        postServico.criar(pLucas.getId(),
            "Treino tático hoje no Campo do Retiro. Trabalhamos marcação por zona e transição defensiva. O time está evoluindo muito a cada semana 💪 Semana que vem tem jogo-treino, bora!",
            "Futebol");

        postServico.criar(pMaria.getId(),
            "Dica que mudou meu treino: beba 500ml de água 2h antes de corridas longas e mais 200ml nos 30min antes. Desde que adotei isso não tive mais cãibras nos treinos acima de 18km 🏃💧",
            "Corrida");

        postServico.criar(pBeatriz.getId(),
            "200km de trilha em 48h no Caminho do Mar esse fim de semana. Cansaço total, mas a sensação de cruzar a linha de chegada não tem preço. A montanha devolve tudo que você investe nela 🏔️",
            "Corrida");

        // ── Licenças no feed (espelha as compras do seed do marketplace) ──────
        // fotos: 1-3=surf/pedro, 4-5=corrida/pedro, 6-8=skate/carla,
        //        9-10=ciclismo/carla, 11-12=surf-pôr/bruno, 13-14=futebol/bruno
        feedServico.publicarItem(pMaria.getId(),  TipoItemFeed.LICENCA_ADQUIRIDA, 1);
        feedServico.publicarItem(pMaria.getId(),  TipoItemFeed.LICENCA_ADQUIRIDA, 4);
        feedServico.publicarItem(pJoao.getId(),   TipoItemFeed.LICENCA_ADQUIRIDA, 2);
        feedServico.publicarItem(pJoao.getId(),   TipoItemFeed.LICENCA_ADQUIRIDA, 11);
        feedServico.publicarItem(pAna.getId(),    TipoItemFeed.LICENCA_ADQUIRIDA, 6);
        feedServico.publicarItem(pAna.getId(),    TipoItemFeed.LICENCA_ADQUIRIDA, 7);
        feedServico.publicarItem(pLucas.getId(),  TipoItemFeed.LICENCA_ADQUIRIDA, 13);
        feedServico.publicarItem(pBeatriz.getId(), TipoItemFeed.LICENCA_ADQUIRIDA, 9);
    }
}

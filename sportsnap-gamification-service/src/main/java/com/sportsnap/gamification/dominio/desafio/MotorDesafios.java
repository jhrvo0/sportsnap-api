package com.sportsnap.gamification.dominio.desafio;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico.CartaSincronizadaEvento;

/**
 * Motor reativo que processa os progressos ao receber eventos de dominio (RN24
 * a RN29, RN31): incrementa contadores conforme os criterios, respeita a meta
 * (RN25), reinicia ciclos periodicos (RN31), expira fora da janela (RN27),
 * conclui quando todos os criterios sao satisfeitos (RN26) e concede a insignia
 * de forma idempotente (RN28), publicando o evento de conclusao (RN29).
 */
public class MotorDesafios {

    private final DesafioRepositorio desafioRepositorio;
    private final ProgressoDesafioRepositorio progressoRepositorio;
    private final InsigniaRepositorio insigniaRepositorio;
    private final EventoBarramento barramento;

    public MotorDesafios(DesafioRepositorio desafioRepositorio,
                         ProgressoDesafioRepositorio progressoRepositorio,
                         InsigniaRepositorio insigniaRepositorio,
                         EventoBarramento barramento) {
        notNull(desafioRepositorio, "O repositorio de Desafio nao pode ser nulo");
        notNull(progressoRepositorio, "O repositorio de Progresso nao pode ser nulo");
        notNull(insigniaRepositorio, "O repositorio de Insignia nao pode ser nulo");
        notNull(barramento, "O barramento de eventos nao pode ser nulo");
        this.desafioRepositorio = desafioRepositorio;
        this.progressoRepositorio = progressoRepositorio;
        this.insigniaRepositorio = insigniaRepositorio;
        this.barramento = barramento;
    }

    /** Reage a uma sincronizacao confirmada, avancando os progressos do atleta (RN24). */
    public void aoSincronizar(CartaSincronizadaEvento evento) {
        notNull(evento, "O evento nao pode ser nulo");
        CartaOficial carta = evento.getCarta();
        AtletaId atletaId = carta.getAtletaId();
        LocalDateTime agora = LocalDateTime.now();

        for (ProgressoDesafio progresso : progressoRepositorio.listarPorAtleta(atletaId)) {
            if (!progresso.estaAtivo()) {
                continue;
            }
            Desafio desafio = desafioRepositorio.obterPorId(progresso.getDesafioId()).orElse(null);
            if (desafio == null) {
                continue;
            }
            reiniciarCicloSeNecessario(progresso, desafio, agora);

            if (!desafio.estaAtivoEm(agora)) {
                progresso.expirar(); // RN27
                progressoRepositorio.salvar(progresso);
                continue;
            }
            aplicarCriterios(progresso, desafio, carta);
            concluirSeCompleto(progresso, desafio, atletaId, agora);
            progressoRepositorio.salvar(progresso);
        }
    }

    private void aplicarCriterios(ProgressoDesafio progresso, Desafio desafio, CartaOficial carta) {
        List<CriterioDesafio> criterios = desafio.getCriterios();
        for (int i = 0; i < criterios.size(); i++) {
            CriterioDesafio criterio = criterios.get(i);
            switch (criterio.getTipo()) {
                case CONTAGEM_SINCRONIZACOES -> progresso.incrementar(i, 1, criterio.getMeta());
                case LIMIAR_OVERALL ->
                    progresso.registrarLimiarAtingido(i, carta.getOverall() >= criterio.getMeta(), criterio.getMeta());
                case LIMIAR_ATRIBUTO ->
                    progresso.registrarLimiarAtingido(i, atingiuAtributo(carta, criterio), criterio.getMeta());
                case POSICAO_RANKING -> {
                    // avaliado por outro gatilho (ranking), nao pela sincronizacao
                }
            }
        }
    }

    private void concluirSeCompleto(ProgressoDesafio progresso, Desafio desafio,
                                    AtletaId atletaId, LocalDateTime agora) {
        if (!progresso.estaCompleto(desafio.metas()) || progresso.isInsigniaConcedida()) {
            return;
        }
        // RN28: concede a insignia exatamente uma vez
        if (!insigniaRepositorio.existePorAtletaEDesafio(atletaId, desafio.getId())) {
            insigniaRepositorio.salvar(new Insignia(atletaId, desafio.getInsigniaCodigo(),
                desafio.getId(), agora));
        }
        progresso.marcarInsigniaConcedida();
        progresso.concluir();
        barramento.postar(new DesafioConcluidoEvento(atletaId, desafio.getId(), desafio.getInsigniaCodigo()));
    }

    private void reiniciarCicloSeNecessario(ProgressoDesafio progresso, Desafio desafio, LocalDateTime agora) {
        LocalDate cicloAtual = cicloAtual(desafio.getCadencia(), agora);
        if (cicloAtual != null && !cicloAtual.equals(progresso.getCicloReferencia())) {
            progresso.reiniciarCiclo(cicloAtual); // RN31
        }
    }

    private boolean atingiuAtributo(CartaOficial carta, CriterioDesafio criterio) {
        return carta.getAtributos().stream()
            .filter(a -> a.getNome().equalsIgnoreCase(criterio.getAlvoAtributo()))
            .mapToDouble(AtributoEsportivo::getValor)
            .anyMatch(v -> v >= criterio.getMeta());
    }

    private LocalDate cicloAtual(Cadencia cadencia, LocalDateTime agora) {
        return switch (cadencia) {
            case DIARIO -> agora.toLocalDate();
            case SEMANAL -> agora.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case NENHUMA -> null;
        };
    }
}

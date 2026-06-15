package com.sportsnap.gamification.dominio.desafio;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sportsnap.gamification.dominio.analise.AnaliseServico;
import com.sportsnap.gamification.dominio.analise.ForcaFraqueza;
import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Casos de uso de definicao e participacao em desafios (RN14 a RN23, RN30,
 * RN32, RN34): definir desafios, sugerir desafio para o ponto fraco do atleta
 * (RN19), aceitar respeitando janela e pre-requisitos, cancelar e listar
 * disponiveis.
 */
public class DesafioServico {

    private final DesafioRepositorio desafioRepositorio;
    private final ProgressoDesafioRepositorio progressoRepositorio;
    private final AnaliseServico analiseServico;

    public DesafioServico(DesafioRepositorio desafioRepositorio,
                          ProgressoDesafioRepositorio progressoRepositorio,
                          AnaliseServico analiseServico) {
        notNull(desafioRepositorio, "O repositorio de Desafio nao pode ser nulo");
        notNull(progressoRepositorio, "O repositorio de Progresso nao pode ser nulo");
        notNull(analiseServico, "O servico de Analise nao pode ser nulo");
        this.desafioRepositorio = desafioRepositorio;
        this.progressoRepositorio = progressoRepositorio;
        this.analiseServico = analiseServico;
    }

    /** Define e persiste um desafio valido (RN14 a RN18). */
    public Desafio definir(Desafio desafio) {
        notNull(desafio, "O desafio nao pode ser nulo");
        return desafioRepositorio.salvar(desafio);
    }

    /** Aceita um desafio criando um progresso zerado (RN20, RN21, RN22, RN23). */
    public ProgressoDesafio aceitar(AtletaId atletaId, int desafioId, LocalDateTime agora) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(agora, "O instante atual nao pode ser nulo");
        Desafio desafio = obterDesafio(desafioId);

        if (!desafio.estaAtivoEm(agora)) {
            throw new IllegalStateException("RN20: desafio so pode ser aceito enquanto ativo");
        }
        Set<Integer> concluidos = desafiosConcluidos(atletaId);
        if (!concluidos.containsAll(desafio.getPrerequisitos())) {
            throw new IllegalStateException("RN21: pre-requisitos do desafio nao atendidos");
        }
        if (progressoRepositorio.obterAtivo(atletaId, desafioId).isPresent()) {
            throw new IllegalStateException("RN22: ja existe progresso ativo para o desafio");
        }
        return progressoRepositorio.salvar(
            new ProgressoDesafio(atletaId, desafioId, desafio.getCriterios().size(), agora));
    }

    /** Cancela um progresso ativo, descartando o avanco parcial (RN30). */
    public ProgressoDesafio cancelar(int progressoId) {
        ProgressoDesafio progresso = progressoRepositorio.obterPorId(progressoId)
            .orElseThrow(() -> new IllegalArgumentException("Progresso nao encontrado: " + progressoId));
        progresso.cancelar();
        return progressoRepositorio.salvar(progresso);
    }

    /** Sugere um desafio direcionado ao atributo mais fraco do atleta (RN19). */
    public Optional<Desafio> sugerirParaPontoFraco(AtletaId atletaId, String modalidade) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notBlank(modalidade, "A modalidade e obrigatoria");
        // O ponto fraco e o atributo mais abaixo da media da modalidade.
        Optional<String> pontoFraco = analiseServico.forcaFraqueza(atletaId, modalidade).stream()
            .min((a, b) -> Double.compare(a.getValor() - a.getMedia(), b.getValor() - b.getMedia()))
            .map(ForcaFraqueza::getAtributo);
        if (pontoFraco.isEmpty()) {
            return Optional.empty();
        }
        String atributo = pontoFraco.get();
        return desafioRepositorio.listarTodos().stream()
            .filter(d -> d.getCriterios().stream().anyMatch(c ->
                c.getTipo() == TipoCriterio.LIMIAR_ATRIBUTO
                    && atributo.equalsIgnoreCase(c.getAlvoAtributo())))
            .findFirst();
    }

    /** Desafios ativos, excluindo concluidos nao-repetiveis e cadeias bloqueadas (RN32, RN34). */
    public List<Desafio> listarDisponiveis(AtletaId atletaId, LocalDateTime agora) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        Set<Integer> concluidos = desafiosConcluidos(atletaId);
        return desafioRepositorio.listarTodos().stream()
            .filter(d -> d.estaAtivoEm(agora))
            .filter(d -> !(concluidos.contains(d.getId()) && !d.isRepetivel()))
            .filter(d -> concluidos.containsAll(d.getPrerequisitos()))
            .toList();
    }

    private Set<Integer> desafiosConcluidos(AtletaId atletaId) {
        return progressoRepositorio.listarPorAtleta(atletaId).stream()
            .filter(p -> p.getStatus() == StatusProgresso.CONCLUIDO)
            .map(ProgressoDesafio::getDesafioId)
            .collect(Collectors.toSet());
    }

    private Desafio obterDesafio(int desafioId) {
        return desafioRepositorio.obterPorId(desafioId)
            .orElseThrow(() -> new IllegalArgumentException("Desafio nao encontrado: " + desafioId));
    }
}

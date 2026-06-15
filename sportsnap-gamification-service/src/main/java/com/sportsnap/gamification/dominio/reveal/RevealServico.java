package com.sportsnap.gamification.dominio.reveal;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.Map;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucao;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico.CartaSincronizadaEvento;

/**
 * Servico de dominio do Reveal estrategico (RN10 a RN24): libera um orcamento de
 * pontos a partir do XP latente e do tier, permite simular a alocacao sem
 * persistir (RN19) e confirma a alocacao de forma irreversivel (RN20),
 * registrando sincronizacao (RN23) e evolucao (RN24) e publicando o evento de
 * dominio (RN22).
 */
public class RevealServico {

    private final CartaOficialRepositorio cartaRepositorio;
    private final StatusPotencialRepositorio statusRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final RegistroSincronizacaoRepositorio sincronizacaoRepositorio;
    private final RegistroEvolucaoRepositorio evolucaoRepositorio;
    private final MotorAlocacao motorAlocacao;
    private final EventoBarramento barramento;

    public RevealServico(CartaOficialRepositorio cartaRepositorio,
                         StatusPotencialRepositorio statusRepositorio,
                         LicencaRepositorio licencaRepositorio,
                         RegistroSincronizacaoRepositorio sincronizacaoRepositorio,
                         RegistroEvolucaoRepositorio evolucaoRepositorio,
                         MotorAlocacao motorAlocacao,
                         EventoBarramento barramento) {
        notNull(cartaRepositorio, "O repositorio de CartaOficial nao pode ser nulo");
        notNull(statusRepositorio, "O repositorio de StatusPotencial nao pode ser nulo");
        notNull(licencaRepositorio, "O repositorio de Licenca nao pode ser nulo");
        notNull(sincronizacaoRepositorio, "O repositorio de RegistroSincronizacao nao pode ser nulo");
        notNull(evolucaoRepositorio, "O repositorio de RegistroEvolucao nao pode ser nulo");
        notNull(motorAlocacao, "O motor de alocacao nao pode ser nulo");
        notNull(barramento, "O barramento de eventos nao pode ser nulo");
        this.cartaRepositorio = cartaRepositorio;
        this.statusRepositorio = statusRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.sincronizacaoRepositorio = sincronizacaoRepositorio;
        this.evolucaoRepositorio = evolucaoRepositorio;
        this.motorAlocacao = motorAlocacao;
        this.barramento = barramento;
    }

    /** Inicia o Reveal liberando o orcamento de pontos, sem alterar estado (RN10, RN11, RN12). */
    public Orcamento iniciar(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        CartaOficial carta = obterCarta(atletaId);
        StatusPotencial status = obterStatus(atletaId);
        validarPreCondicoes(atletaId, carta, status);
        return Orcamento.calcular(status.getXpAcumulado(), carta.getTier(), carta.getSaldoPontos());
    }

    /** Previa nao-destrutiva da alocacao informada (RN19). */
    public Simulacao simular(AtletaId atletaId, Map<String, Integer> incrementos) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(incrementos, "A alocacao nao pode ser nula");
        CartaOficial carta = obterCarta(atletaId);
        StatusPotencial status = obterStatus(atletaId);
        validarPreCondicoes(atletaId, carta, status);

        Orcamento orcamento = Orcamento.calcular(status.getXpAcumulado(), carta.getTier(), carta.getSaldoPontos());
        motorAlocacao.validar(carta, incrementos, orcamento);
        int custo = motorAlocacao.custoTotal(carta, incrementos);

        CartaOficial copia = copiarCarta(carta);
        copia.aplicarAlocacao(incrementos);
        return new Simulacao(carta.getOverall(), copia.getOverall(),
            carta.getTier(), copia.getTier(), custo, orcamento.getPontosDisponiveis() - custo);
    }

    /** Confirma a alocacao de forma irreversivel (RN13 a RN24). */
    public RegistroSincronizacao confirmar(AtletaId atletaId, Map<String, Integer> incrementos) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(incrementos, "A alocacao nao pode ser nula");
        CartaOficial carta = obterCarta(atletaId);
        StatusPotencial status = obterStatus(atletaId);
        validarPreCondicoes(atletaId, carta, status);

        Orcamento orcamento = Orcamento.calcular(status.getXpAcumulado(), carta.getTier(), carta.getSaldoPontos());
        motorAlocacao.validar(carta, incrementos, orcamento);
        int custo = motorAlocacao.custoTotal(carta, incrementos);

        double overallAnterior = carta.getOverall();
        carta.consumirSaldo();
        carta.aplicarAlocacao(incrementos);
        carta.adicionarSaldo(orcamento.getPontosDisponiveis() - custo);

        double xpConvertido = status.zerar();
        statusRepositorio.salvar(status);
        cartaRepositorio.salvar(carta);

        LocalDateTime agora = LocalDateTime.now();
        evolucaoRepositorio.inserir(new RegistroEvolucao(
            atletaId, overallAnterior, carta.getOverall(), agora));
        RegistroSincronizacao registro = sincronizacaoRepositorio.salvar(new RegistroSincronizacao(
            atletaId, agora, orcamento.getPontosDisponiveis(), custo, overallAnterior, carta.getOverall(), incrementos));

        barramento.postar(new CartaSincronizadaEvento(carta, xpConvertido));
        return registro;
    }

    private void validarPreCondicoes(AtletaId atletaId, CartaOficial carta, StatusPotencial status) {
        if (carta.isArquivada()) {
            throw new IllegalStateException("RN25: carta arquivada nao aceita Reveal");
        }
        if (status.getXpAcumulado() <= 0) {
            throw new IllegalStateException("RN10: Reveal exige potencial latente positivo");
        }
        if (!possuiLicencaValida(atletaId, carta)) {
            throw new IllegalStateException("RN11: Reveal exige licenca valida posterior a ultima sincronizacao");
        }
    }

    private boolean possuiLicencaValida(AtletaId atletaId, CartaOficial carta) {
        LocalDateTime ultimaSync = carta.getUltimaSincronizacao();
        if (ultimaSync == null) {
            return !licencaRepositorio.listarPorAtleta(atletaId).isEmpty();
        }
        return licencaRepositorio.existeLicencaPosterior(atletaId, ultimaSync);
    }

    private CartaOficial copiarCarta(CartaOficial carta) {
        var copiaAtributos = carta.getAtributos().stream()
            .map(a -> new AtributoEsportivo(a.getNome(), a.getValor(), a.getPeso(), a.getTipoEsporte()))
            .toList();
        return new CartaOficial(carta.getAtletaId(), copiaAtributos, carta.getOverall(),
            carta.getUltimaSincronizacao(), carta.getTier(), carta.getSaldoPontos(), carta.isArquivada());
    }

    private CartaOficial obterCarta(AtletaId atletaId) {
        return cartaRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("CartaOficial nao encontrada: " + atletaId));
    }

    private StatusPotencial obterStatus(AtletaId atletaId) {
        return statusRepositorio.obterPorAtleta(atletaId)
            .orElseThrow(() -> new IllegalStateException("StatusPotencial nao encontrado: " + atletaId));
    }
}

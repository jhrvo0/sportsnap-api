package com.sportsnap.gamification.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.aplicacao.atleta.AtletaResumo;
import com.sportsnap.gamification.aplicacao.atleta.AtletaServicoAplicacao;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.atleta.AtletaServico;
import com.sportsnap.gamification.dominio.atleta.Email;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacaoRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico.CartaSincronizadaEvento;

@RestController
@RequestMapping("/api/atletas")
public class AtletaControlador {

    @Autowired private AtletaServico atletaServico;
    @Autowired private AtletaServicoAplicacao atletaServicoAplicacao;
    @Autowired private SincronizacaoServico sincronizacaoServico;
    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private RegistroEvolucaoRepositorio evolucaoRepositorio;
    @Autowired private RegistroSincronizacaoRepositorio sincronizacaoRepositorio;
    @Autowired private EventoBarramento barramento;

    @GetMapping
    public List<AtletaResumo> listar() {
        return atletaServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public void cadastrar(@RequestBody AtletaDto dto) {
        atletaServico.cadastrar(dto.nome, new Email(dto.email));
    }

    @PostMapping("/{id}/sincronizar")
    public void sincronizar(@PathVariable int id) {
        sincronizacaoServico.sincronizar(new AtletaId(id));
    }

    /**
     * Registra um "treino" e dispara o evento de carta sincronizada, que alimenta
     * o Motor de Desafios. Util para a demo avancar desafios sem depender de XP.
     */
    @PostMapping("/{id}/registrar-treino")
    public void registrarTreino(@PathVariable int id) {
        CartaOficial carta = cartaRepositorio.obterPorAtleta(new AtletaId(id))
            .orElseThrow(() -> new IllegalArgumentException("Carta nao encontrada: " + id));
        barramento.postar(new CartaSincronizadaEvento(carta, 0));
    }

    /** Carta detalhada do atleta: tier, saldo, overall e atributos (F1). */
    @GetMapping("/{id}/carta")
    public CartaDto carta(@PathVariable int id) {
        CartaOficial carta = cartaRepositorio.obterPorAtleta(new AtletaId(id))
            .orElseThrow(() -> new IllegalArgumentException("Carta nao encontrada: " + id));
        List<AtributoDto> atributos = carta.getAtributos().stream()
            .map(a -> new AtributoDto(a.getNome(), a.getValor(), a.getPeso(), a.getTipoEsporte()))
            .toList();
        return new CartaDto(carta.getAtletaId().getId(), carta.getOverall(), carta.getTier().name(),
            carta.getSaldoPontos(), carta.isArquivada(), carta.isSincronizada(),
            carta.getUltimaSincronizacao(), atributos);
    }

    /** Historico de evolucao do Overall (RN24). */
    @GetMapping("/{id}/evolucao")
    public List<EvolucaoDto> evolucao(@PathVariable int id) {
        return evolucaoRepositorio.listarPorAtleta(new AtletaId(id)).stream()
            .map(r -> new EvolucaoDto(r.getOverallAnterior(), r.getOverallNovo(), r.getDelta(), r.getOcorridoEm()))
            .toList();
    }

    /** Historico de sincronizacoes (Reveals) confirmadas (RN23). */
    @GetMapping("/{id}/sincronizacoes")
    public List<SincronizacaoDto> sincronizacoes(@PathVariable int id) {
        return sincronizacaoRepositorio.listarPorAtleta(new AtletaId(id)).stream()
            .map(r -> new SincronizacaoDto(r.getOcorridoEm(), r.getOrcamentoPontos(), r.getCustoTotal(),
                r.getOverallAnterior(), r.getOverallNovo(), r.getVariacaoOverall()))
            .toList();
    }

    public static class AtletaDto {
        public String nome;
        public String email;
    }

    public record AtributoDto(String nome, double valor, double peso, String tipoEsporte) {}

    public record CartaDto(int atletaId, double overall, String tier, double saldoPontos, boolean arquivada,
                           boolean sincronizada, LocalDateTime ultimaSincronizacao, List<AtributoDto> atributos) {}

    public record EvolucaoDto(double overallAnterior, double overallNovo, double delta, LocalDateTime ocorridoEm) {}

    public record SincronizacaoDto(LocalDateTime ocorridoEm, int orcamentoPontos, int custoTotal,
                                   double overallAnterior, double overallNovo, double variacaoOverall) {}
}

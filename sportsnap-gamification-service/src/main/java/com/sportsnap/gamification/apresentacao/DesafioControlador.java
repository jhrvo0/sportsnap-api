package com.sportsnap.gamification.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.desafio.Cadencia;
import com.sportsnap.gamification.dominio.desafio.CriterioDesafio;
import com.sportsnap.gamification.dominio.desafio.Desafio;
import com.sportsnap.gamification.dominio.desafio.DesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.DesafioServico;
import com.sportsnap.gamification.dominio.desafio.InsigniaRepositorio;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafio;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.TipoCriterio;

@RestController
@RequestMapping("/api/desafios")
public class DesafioControlador {

    @Autowired private DesafioServico desafioServico;
    @Autowired private DesafioRepositorio desafioRepositorio;
    @Autowired private ProgressoDesafioRepositorio progressoRepositorio;
    @Autowired private InsigniaRepositorio insigniaRepositorio;

    @GetMapping
    public List<DesafioDto> listar() {
        return desafioRepositorio.listarTodos().stream().map(this::paraDto).toList();
    }

    @PostMapping
    public DesafioDto definir(@RequestBody DesafioRequest req) {
        List<CriterioDesafio> criterios = req.criterios.stream()
            .map(c -> new CriterioDesafio(TipoCriterio.valueOf(c.tipo), c.meta, c.alvoAtributo))
            .toList();
        Cadencia cadencia = Cadencia.valueOf(req.cadencia == null ? "NENHUMA" : req.cadencia);
        Desafio desafio = desafioServico.definir(new Desafio(req.titulo, criterios, req.inicio, req.fim,
            req.permanente, req.insigniaCodigo, req.prerequisitos, cadencia, req.repetivel));
        return paraDto(desafio);
    }

    @GetMapping("/disponiveis")
    public List<DesafioDto> disponiveis(@RequestParam int atletaId) {
        return desafioServico.listarDisponiveis(new AtletaId(atletaId), LocalDateTime.now()).stream()
            .map(this::paraDto).toList();
    }

    @PostMapping("/{id}/aceitar")
    public ProgressoDto aceitar(@PathVariable int id, @RequestParam int atletaId) {
        return paraProgressoDto(desafioServico.aceitar(new AtletaId(atletaId), id, LocalDateTime.now()));
    }

    @PostMapping("/progressos/{progressoId}/cancelar")
    public ProgressoDto cancelar(@PathVariable int progressoId) {
        return paraProgressoDto(desafioServico.cancelar(progressoId));
    }

    @GetMapping("/progressos")
    public List<ProgressoDto> progressos(@RequestParam int atletaId) {
        return progressoRepositorio.listarPorAtleta(new AtletaId(atletaId)).stream()
            .map(this::paraProgressoDto).toList();
    }

    @GetMapping("/insignias")
    public List<InsigniaDto> insignias(@RequestParam int atletaId) {
        return insigniaRepositorio.listarPorAtleta(new AtletaId(atletaId)).stream()
            .map(i -> new InsigniaDto(i.getCodigo(), i.getDesafioId(), i.getConcedidaEm()))
            .toList();
    }

    @GetMapping("/sugestao")
    public DesafioDto sugestao(@RequestParam int atletaId, @RequestParam String modalidade) {
        return desafioServico.sugerirParaPontoFraco(new AtletaId(atletaId), modalidade)
            .map(this::paraDto)
            .orElse(null);
    }

    private DesafioDto paraDto(Desafio d) {
        return new DesafioDto(d.getId(), d.getTitulo(), d.getInsigniaCodigo(),
            d.isPermanente(), d.getCadencia().name(), d.getCriterios().size());
    }

    private ProgressoDto paraProgressoDto(ProgressoDesafio p) {
        Desafio d = desafioRepositorio.obterPorId(p.getDesafioId()).orElse(null);
        String titulo = d != null ? d.getTitulo() : "Desafio #" + p.getDesafioId();
        double percentual = d != null ? p.percentualConcluido(d.metas()) : 0;
        String insignia = d != null ? d.getInsigniaCodigo() : null;
        return new ProgressoDto(p.getId(), p.getAtletaId().getId(), p.getDesafioId(), titulo,
            p.getStatus().name(), percentual, p.isInsigniaConcedida(), insignia);
    }

    public static class DesafioRequest {
        public String titulo;
        public List<CriterioRequest> criterios;
        public LocalDateTime inicio;
        public LocalDateTime fim;
        public boolean permanente;
        public String insigniaCodigo;
        public List<Integer> prerequisitos;
        public String cadencia;
        public boolean repetivel;
    }

    public static class CriterioRequest {
        public String tipo;
        public int meta;
        public String alvoAtributo;
    }

    public record DesafioDto(Integer id, String titulo, String insigniaCodigo, boolean permanente,
                             String cadencia, int numeroCriterios) {}

    public record ProgressoDto(Integer id, int atletaId, int desafioId, String titulo, String status,
                               double percentual, boolean insigniaConcedida, String insigniaCodigo) {}

    public record InsigniaDto(String codigo, int desafioId, LocalDateTime concedidaEm) {}
}

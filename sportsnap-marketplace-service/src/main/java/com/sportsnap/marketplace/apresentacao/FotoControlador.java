package com.sportsnap.marketplace.apresentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.marketplace.aplicacao.foto.FotoServicoAplicacao;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.lote.LoteId;

@RestController
@RequestMapping("/api/fotos")
public class FotoControlador {

    @Autowired private FotoServicoAplicacao fotoServicoAplicacao;
    @Autowired private FotoServico fotoServico;
    @Autowired private FotoRepositorio fotoRepositorio;

    @GetMapping
    public List<FotoDto> listar(@RequestParam(required = false) Integer loteId) {
        if (loteId != null) {
            return fotoServico.listarPorLote(new LoteId(loteId)).stream().map(this::toDto).toList();
        }
        return fotoRepositorio.listarTodas().stream()
            .filter(f -> !f.isRemovida())
            .map(this::toDto)
            .toList();
    }

    @GetMapping("/{id}")
    public FotoDto obter(@PathVariable int id) {
        return toDto(fotoServico.obter(new FotoId(id)));
    }

    @PostMapping
    public List<FotoDto> upload(@RequestBody UploadDto dto) {
        var uploads = dto.fotos.stream()
            .map(f -> new FotoServico.FotoUpload(f.nome, f.urlPreview))
            .toList();
        return fotoServico.uploadEmLoteComPreview(new LoteId(dto.loteId), uploads).stream()
            .map(this::toDto)
            .toList();
    }

    @PostMapping("/{id}/remover")
    public void remover(@PathVariable int id) {
        fotoServico.remover(new FotoId(id));
    }

    @PutMapping("/{id}/preco")
    public FotoDto definirPreco(@PathVariable int id, @RequestBody PrecoDto dto) {
        fotoServico.definirPreco(new FotoId(id), dto.preco);
        return toDto(fotoServico.obter(new FotoId(id)));
    }

    @PostMapping("/{id}/disponibilizar")
    public FotoDto disponibilizar(@PathVariable int id) {
        fotoServico.disponibilizar(new FotoId(id));
        return toDto(fotoServico.obter(new FotoId(id)));
    }

    @PostMapping("/{id}/indisponibilizar")
    public FotoDto indisponibilizar(@PathVariable int id) {
        fotoServico.indisponibilizar(new FotoId(id));
        return toDto(fotoServico.obter(new FotoId(id)));
    }

    private FotoDto toDto(Foto f) {
        return new FotoDto(
            f.getId().getId(),
            f.getLoteId().getId(),
            f.getUrlPreview(),
            f.getUrlOriginal(),
            f.getExif().getTimestamp(),
            f.getExif().getDetalhes(),
            f.isLicenciada(),
            f.isRemovida(),
            f.getPreco().getValor(),
            f.isDisponivel()
        );
    }

    public static class UploadDto {
        public int loteId;
        public List<FotoItem> fotos;

        public static class FotoItem {
            public String nome;
            public String urlPreview;
        }
    }

    public static class PrecoDto {
        public BigDecimal preco;
    }

    public record FotoDto(
        int id,
        int loteId,
        String urlPreview,
        String urlOriginal,
        LocalDateTime exifTimestamp,
        String exifDetalhes,
        boolean licenciada,
        boolean removida,
        BigDecimal preco,
        boolean disponivel
    ) {}
}

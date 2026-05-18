package com.sportsnap.marketplace.apresentacao;

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

import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.dominio.lote.LoteServico;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;

@RestController
@RequestMapping("/api/lotes")
public class LoteControlador {

    @Autowired private LoteServico loteServico;

    @GetMapping
    public List<LoteDto> listar(@RequestParam(required = false) Integer fotografoId) {
        if (fotografoId == null) {
            return List.of();
        }
        return loteServico.listarPorFotografo(new FotografoId(fotografoId)).stream()
            .map(this::toDto)
            .toList();
    }

    @PostMapping
    public LoteDto criar(@RequestBody LoteCreateDto dto) {
        var lote = loteServico.cadastrar(
            new FotografoId(dto.fotografoId),
            new SessaoId(dto.sessaoId),
            new SpotId(dto.spotId),
            dto.descricao
        );
        return toDto(lote);
    }

    @PostMapping("/{id}/arquivar")
    public void arquivar(@PathVariable int id) {
        loteServico.arquivar(new LoteId(id));
    }

    private LoteDto toDto(Lote l) {
        return new LoteDto(
            l.getId().getId(),
            l.getFotografoId().getId(),
            l.getSessaoId().getId(),
            l.getSpotId().getId(),
            l.getDescricao(),
            l.getCriadoEm(),
            l.isArquivado()
        );
    }

    public static class LoteCreateDto {
        public int fotografoId;
        public int sessaoId;
        public int spotId;
        public String descricao;
    }

    public record LoteDto(
        int id,
        int fotografoId,
        int sessaoId,
        int spotId,
        String descricao,
        LocalDateTime criadoEm,
        boolean arquivado
    ) {}
}

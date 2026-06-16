package com.sportsnap.marketplace.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.sugestao.MotorSugestaoServico;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoControlador {

    @Autowired private MotorSugestaoServico motorSugestaoServico;
    @Autowired private FotoRepositorio fotoRepositorio;

    @PostMapping
    public void favoritar(@RequestBody FavoritoDto dto) {
        motorSugestaoServico.favoritar(new AtletaId(dto.atletaId), new FotoId(dto.fotoId));
    }

    @DeleteMapping
    public void desfavoritar(@RequestBody FavoritoDto dto) {
        motorSugestaoServico.desfavoritar(new AtletaId(dto.atletaId), new FotoId(dto.fotoId));
    }

    @GetMapping("/{atletaId}")
    public List<FotoResumoDto> listarFavoritos(@PathVariable int atletaId) {
        List<FotoId> ids = motorSugestaoServico.listarFavoritos(new AtletaId(atletaId));
        return ids.stream()
            .flatMap(id -> fotoRepositorio.obter(id).stream())
            .filter(f -> !f.isRemovida())
            .map(this::toResumo)
            .toList();
    }

    private FotoResumoDto toResumo(Foto f) {
        return new FotoResumoDto(
            f.getId().getId(),
            f.getLoteId().getId(),
            f.getUrlPreview(),
            f.getExif().getTimestamp().toString(),
            f.isLicenciada()
        );
    }

    public static class FavoritoDto {
        public int atletaId;
        public int fotoId;
    }

    public record FotoResumoDto(
        int id,
        int loteId,
        String urlPreview,
        String exifTimestamp,
        boolean licenciada
    ) {}
}

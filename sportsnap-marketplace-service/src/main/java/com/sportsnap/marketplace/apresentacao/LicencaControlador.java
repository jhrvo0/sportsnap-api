package com.sportsnap.marketplace.apresentacao;

import java.math.BigDecimal;
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

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaId;
import com.sportsnap.marketplace.dominio.licenca.SplitFinanceiro;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;

@RestController
@RequestMapping("/api/licencas")
public class LicencaControlador {

    @Autowired private VendaServico vendaServico;

    @PostMapping
    public LicencaDto comprar(@RequestBody CompraDto dto) {
        var licenca = vendaServico.processarVenda(new AtletaId(dto.atletaId), new FotoId(dto.fotoId));
        return toDto(licenca);
    }

    @GetMapping
    public List<LicencaDto> listarPorAtleta(@RequestParam int atletaId) {
        return vendaServico.listarPorAtleta(new AtletaId(atletaId)).stream()
            .map(this::toDto)
            .toList();
    }

    @GetMapping("/{id}/split")
    public SplitDto obterSplit(@PathVariable int id) {
        SplitFinanceiro split = vendaServico.obterSplit(new LicencaId(id));
        return new SplitDto(
            split.getLicencaId().getId(),
            split.getValorFotografo().getValor(),
            split.getTaxaPlataforma().getValor(),
            split.getProcessadoEm()
        );
    }

    @PostMapping("/{id}/cancelar")
    public void cancelar(@PathVariable int id) {
        vendaServico.cancelar(new LicencaId(id));
    }

    private LicencaDto toDto(LicencaDeImagem l) {
        return new LicencaDto(
            l.getId().getId(),
            l.getAtletaId().getId(),
            l.getFotoId().getId(),
            l.getPreco().getValor(),
            l.getAdquiridaEm(),
            l.isCancelada()
        );
    }

    public static class CompraDto {
        public int atletaId;
        public int fotoId;
    }

    public record LicencaDto(
        int id,
        int atletaId,
        int fotoId,
        BigDecimal preco,
        LocalDateTime adquiridaEm,
        boolean cancelada
    ) {}

    public record SplitDto(
        int licencaId,
        BigDecimal valorFotografo,
        BigDecimal taxaPlataforma,
        LocalDateTime processadoEm
    ) {}
}

package com.sportsnap.marketplace.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.MetadadosExif;
import com.sportsnap.marketplace.dominio.licenca.Dinheiro;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaId;
import com.sportsnap.marketplace.dominio.licenca.SplitFinanceiro;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;
import com.sportsnap.marketplace.dominio.atleta.AtletaId;

@Component
class JpaMapeador {

    Fotografo paraDominio(FotografoJpa jpa) {
        return new Fotografo(new FotografoId(jpa.id), jpa.nome, new Email(jpa.email));
    }

    FotografoJpa paraJpa(Fotografo dominio) {
        var jpa = new FotografoJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.nome = dominio.getNome();
        jpa.email = dominio.getEmail().getEndereco();
        return jpa;
    }

    Lote paraDominio(LoteJpa jpa) {
        return new Lote(new LoteId(jpa.id), new FotografoId(jpa.fotografoId),
            new SessaoId(jpa.sessaoId), new SpotId(jpa.spotId),
            jpa.descricao, jpa.criadoEm, jpa.arquivado);
    }

    LoteJpa paraJpa(Lote dominio) {
        var jpa = new LoteJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.fotografoId = dominio.getFotografoId().getId();
        jpa.sessaoId = dominio.getSessaoId().getId();
        jpa.spotId = dominio.getSpotId().getId();
        jpa.descricao = dominio.getDescricao();
        jpa.criadoEm = dominio.getCriadoEm();
        jpa.arquivado = dominio.isArquivado();
        return jpa;
    }

    Foto paraDominio(FotoJpa jpa) {
        var exif = new MetadadosExif(jpa.exifTimestamp, jpa.exifDetalhes);
        return new Foto(new FotoId(jpa.id), new LoteId(jpa.loteId),
            jpa.urlPreview, jpa.urlOriginal, exif, jpa.licenciada, jpa.removida);
    }

    FotoJpa paraJpa(Foto dominio) {
        var jpa = new FotoJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.loteId = dominio.getLoteId().getId();
        jpa.urlPreview = dominio.getUrlPreview();
        jpa.urlOriginal = dominio.getUrlOriginal();
        jpa.exifTimestamp = dominio.getExif().getTimestamp();
        jpa.exifDetalhes = dominio.getExif().getDetalhes();
        jpa.licenciada = dominio.isLicenciada();
        jpa.removida = dominio.isRemovida();
        return jpa;
    }

    LicencaDeImagem paraDominio(LicencaDeImagemJpa jpa) {
        return new LicencaDeImagem(new LicencaId(jpa.id), new AtletaId(jpa.atletaId),
            new FotoId(jpa.fotoId), new Dinheiro(jpa.preco), jpa.adquiridaEm, jpa.cancelada);
    }

    LicencaDeImagemJpa paraJpa(LicencaDeImagem dominio) {
        var jpa = new LicencaDeImagemJpa();
        if (dominio.getId() != null) {
            jpa.id = dominio.getId().getId();
        }
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.fotoId = dominio.getFotoId().getId();
        jpa.preco = dominio.getPreco().getValor();
        jpa.adquiridaEm = dominio.getAdquiridaEm();
        jpa.cancelada = dominio.isCancelada();
        return jpa;
    }

    SplitFinanceiro paraDominio(SplitFinanceiroJpa jpa) {
        return new SplitFinanceiro(new LicencaId(jpa.licencaId),
            new Dinheiro(jpa.valorFotografo), new Dinheiro(jpa.taxaPlataforma));
    }

    SplitFinanceiroJpa paraJpa(SplitFinanceiro dominio) {
        var jpa = new SplitFinanceiroJpa();
        jpa.licencaId = dominio.getLicencaId().getId();
        jpa.valorFotografo = dominio.getValorFotografo().getValor();
        jpa.taxaPlataforma = dominio.getTaxaPlataforma().getValor();
        jpa.processadoEm = dominio.getProcessadoEm();
        return jpa;
    }
}

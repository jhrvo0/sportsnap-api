package com.sportsnap.marketplace.dominio.sugestao;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MotorSugestaoServico {

    private final FotoRepositorio fotoRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final FavoritoRepositorio favoritoRepositorio;
    private final com.sportsnap.marketplace.dominio.lote.LoteRepositorio loteRepositorio;

    public MotorSugestaoServico(FotoRepositorio fotoRepositorio,
                                  LicencaRepositorio licencaRepositorio,
                                  FavoritoRepositorio favoritoRepositorio,
                                  com.sportsnap.marketplace.dominio.lote.LoteRepositorio loteRepositorio) {
        notNull(fotoRepositorio, "O repositorio de Foto nao pode ser nulo");
        notNull(licencaRepositorio, "O repositorio de Licenca nao pode ser nulo");
        notNull(favoritoRepositorio, "O repositorio de Favoritos nao pode ser nulo");
        notNull(loteRepositorio, "O repositorio de Lote nao pode ser nulo");
        this.fotoRepositorio = fotoRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.favoritoRepositorio = favoritoRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    public List<Foto> sugerirParaAtleta(AtletaId atletaId, List<JanelaCheckIn> janelas) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(janelas, "A lista de janelas nao pode ser nula");

        if (janelas.isEmpty()) {
            return List.of();
        }

        List<com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem> licencasAtivas = licencaRepositorio.listarPorAtleta(atletaId).stream()
                .filter(l -> !l.isCancelada())
                .toList();

        if (licencasAtivas.isEmpty()) {
            return List.of();
        }

        Set<FotoId> jaAdquiridas = new HashSet<>();
        Set<com.sportsnap.marketplace.dominio.lote.SpotId> spotsAdquiridos = new HashSet<>();
        Set<com.sportsnap.marketplace.dominio.fotografo.FotografoId> fotografosAdquiridos = new HashSet<>();

        for (var licenca : licencasAtivas) {
            jaAdquiridas.add(licenca.getFotoId());
            fotoRepositorio.obter(licenca.getFotoId()).ifPresent(foto -> {
                loteRepositorio.obter(foto.getLoteId()).ifPresent(lote -> {
                    spotsAdquiridos.add(lote.getSpotId());
                    fotografosAdquiridos.add(lote.getFotografoId());
                });
            });
        }

        List<FotoPontuada> fotosPontuadas = new ArrayList<>();
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();

        for (Foto foto : fotoRepositorio.listarTodas()) {
            if (foto.isRemovida() || jaAdquiridas.contains(foto.getId())) {
                continue;
            }
            var timestamp = foto.getExif().getTimestamp();
            boolean casaAlgumaJanela = janelas.stream().anyMatch(j -> j.contem(timestamp));
            if (!casaAlgumaJanela) {
                continue;
            }

            double pontuacao = 1.0;
            var loteOpt = loteRepositorio.obter(foto.getLoteId());
            if (loteOpt.isPresent()) {
                var lote = loteOpt.get();
                if (spotsAdquiridos.contains(lote.getSpotId())) {
                    pontuacao += 10.0;
                }
                if (fotografosAdquiridos.contains(lote.getFotografoId())) {
                    pontuacao += 5.0;
                }
            }

            if (timestamp.isAfter(agora.minusHours(48))) {
                pontuacao *= 1.5;
            }

            fotosPontuadas.add(new FotoPontuada(foto, pontuacao));
        }

        return fotosPontuadas.stream()
                .sorted(java.util.Comparator.comparingDouble(FotoPontuada::getPontuacao).reversed())
                .limit(10)
                .map(FotoPontuada::getFoto)
                .toList();
    }

    private static class FotoPontuada {
        private final Foto foto;
        private final double pontuacao;

        public FotoPontuada(Foto foto, double pontuacao) {
            this.foto = foto;
            this.pontuacao = pontuacao;
        }

        public Foto getFoto() { return foto; }
        public double getPontuacao() { return pontuacao; }
    }

    public void favoritar(AtletaId atletaId, FotoId fotoId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(fotoId, "O id da Foto nao pode ser nulo");
        fotoRepositorio.obter(fotoId)
            .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada: " + fotoId));
        favoritoRepositorio.adicionar(atletaId, fotoId);
    }

    public void desfavoritar(AtletaId atletaId, FotoId fotoId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(fotoId, "O id da Foto nao pode ser nulo");
        favoritoRepositorio.remover(atletaId, fotoId);
    }

    public List<FotoId> listarFavoritos(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        return favoritoRepositorio.listarPorAtleta(atletaId);
    }
}

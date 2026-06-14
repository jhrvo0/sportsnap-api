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

    public MotorSugestaoServico(FotoRepositorio fotoRepositorio,
                                  LicencaRepositorio licencaRepositorio,
                                  FavoritoRepositorio favoritoRepositorio) {
        notNull(fotoRepositorio, "O repositorio de Foto nao pode ser nulo");
        notNull(licencaRepositorio, "O repositorio de Licenca nao pode ser nulo");
        notNull(favoritoRepositorio, "O repositorio de Favoritos nao pode ser nulo");
        this.fotoRepositorio = fotoRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.favoritoRepositorio = favoritoRepositorio;
    }

    public List<Foto> sugerirParaAtleta(AtletaId atletaId, List<JanelaCheckIn> janelas) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(janelas, "A lista de janelas nao pode ser nula");

        if (janelas.isEmpty()) {
            return List.of();
        }

        Set<FotoId> jaAdquiridas = new HashSet<>();
        for (var licenca : licencaRepositorio.listarPorAtleta(atletaId)) {
            if (!licenca.isCancelada()) {
                jaAdquiridas.add(licenca.getFotoId());
            }
        }

        List<Foto> sugeridas = new ArrayList<>();
        for (Foto foto : fotoRepositorio.listarTodas()) {
            if (foto.isRemovida()) {
                continue;
            }
            if (jaAdquiridas.contains(foto.getId())) {
                continue;
            }
            var timestamp = foto.getExif().getTimestamp();
            boolean casaAlgumaJanela = janelas.stream().anyMatch(j -> j.contem(timestamp));
            if (casaAlgumaJanela) {
                sugeridas.add(foto);
            }
        }
        return sugeridas;
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

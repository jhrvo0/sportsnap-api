package com.sportsnap.marketplace.dominio.dashboard;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;
import com.sportsnap.marketplace.dominio.licenca.Dinheiro;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;

import java.util.List;

public class DashboardServico {

    private final FotografoRepositorio fotografoRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final FotoRepositorio fotoRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final SplitRepositorio splitRepositorio;

    public DashboardServico(FotografoRepositorio fotografoRepositorio,
                             LoteRepositorio loteRepositorio,
                             FotoRepositorio fotoRepositorio,
                             LicencaRepositorio licencaRepositorio,
                             SplitRepositorio splitRepositorio) {
        notNull(fotografoRepositorio, "O repositorio de Fotografo nao pode ser nulo");
        notNull(loteRepositorio, "O repositorio de Lote nao pode ser nulo");
        notNull(fotoRepositorio, "O repositorio de Foto nao pode ser nulo");
        notNull(licencaRepositorio, "O repositorio de Licenca nao pode ser nulo");
        notNull(splitRepositorio, "O repositorio de Split nao pode ser nulo");
        this.fotografoRepositorio = fotografoRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.fotoRepositorio = fotoRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.splitRepositorio = splitRepositorio;
    }

    public ResumoFotografo consultarResumo(FotografoId fotografoId) {
        notNull(fotografoId, "O id do Fotografo nao pode ser nulo");
        fotografoRepositorio.obter(fotografoId)
            .orElseThrow(() -> new IllegalArgumentException("Fotografo nao encontrado: " + fotografoId));

        List<Lote> lotes = loteRepositorio.listarPorFotografo(fotografoId);

        int totalFotos = 0;
        int totalVendas = 0;
        Dinheiro receita = Dinheiro.ZERO;
        Dinheiro saldo = Dinheiro.ZERO;

        for (Lote lote : lotes) {
            var fotos = fotoRepositorio.listarPorLote(lote.getId());
            totalFotos += fotos.size();

            for (var foto : fotos) {
                for (LicencaDeImagem licenca : licencaRepositorio.listarPorFoto(foto.getId())) {
                    if (licenca.isCancelada()) {
                        continue;
                    }
                    totalVendas++;
                    receita = receita.somar(licenca.getPreco());
                    var split = splitRepositorio.obterPorLicenca(licenca.getId());
                    if (split.isPresent()) {
                        saldo = saldo.somar(split.get().getValorFotografo());
                    }
                }
            }
        }

        return new ResumoFotografo(fotografoId, lotes.size(), totalFotos, totalVendas, receita, saldo);
    }
}

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DashboardServico {

    private final FotografoRepositorio fotografoRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final FotoRepositorio fotoRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final SplitRepositorio splitRepositorio;
    private final ExecutorService executor;

    public DashboardServico(FotografoRepositorio fotografoRepositorio,
                             LoteRepositorio loteRepositorio,
                             FotoRepositorio fotoRepositorio,
                             LicencaRepositorio licencaRepositorio,
                             SplitRepositorio splitRepositorio,
                             ExecutorService executor) {
        notNull(fotografoRepositorio, "O repositorio de Fotografo nao pode ser nulo");
        notNull(loteRepositorio, "O repositorio de Lote nao pode ser nulo");
        notNull(fotoRepositorio, "O repositorio de Foto nao pode ser nulo");
        notNull(licencaRepositorio, "O repositorio de Licenca nao pode ser nulo");
        notNull(splitRepositorio, "O repositorio de Split nao pode ser nulo");
        notNull(executor, "O executor nao pode ser nulo");
        this.fotografoRepositorio = fotografoRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.fotoRepositorio = fotoRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.splitRepositorio = splitRepositorio;
        this.executor = executor;
    }

    public ResumoFotografo consultarResumo(FotografoId fotografoId) {
        notNull(fotografoId, "O id do Fotografo nao pode ser nulo");
        fotografoRepositorio.obter(fotografoId)
            .orElseThrow(() -> new IllegalArgumentException("Fotografo nao encontrado: " + fotografoId));

        List<Lote> lotes = loteRepositorio.listarPorFotografo(fotografoId);

        // Regiões críticas: AtomicInteger e AtomicReference garantem acesso seguro entre threads
        AtomicInteger totalFotos = new AtomicInteger(0);
        AtomicInteger totalVendas = new AtomicInteger(0);
        AtomicReference<Dinheiro> receita = new AtomicReference<>(Dinheiro.ZERO);
        AtomicReference<Dinheiro> saldo = new AtomicReference<>(Dinheiro.ZERO);

        List<CompletableFuture<Void>> tarefas = lotes.stream()
            .map(lote -> CompletableFuture.runAsync(() -> {
                var fotos = fotoRepositorio.listarPorLote(lote.getId());
                totalFotos.addAndGet(fotos.size());

                for (var foto : fotos) {
                    for (LicencaDeImagem licenca : licencaRepositorio.listarPorFoto(foto.getId())) {
                        if (licenca.isCancelada()) continue;
                        totalVendas.incrementAndGet();
                        receita.accumulateAndGet(licenca.getPreco(), Dinheiro::somar);
                        splitRepositorio.obterPorLicenca(licenca.getId()).ifPresent(split ->
                            saldo.accumulateAndGet(split.getValorFotografo(), Dinheiro::somar)
                        );
                    }
                }
            }, executor))
            .toList();

        CompletableFuture.allOf(tarefas.toArray(new CompletableFuture[0])).join();

        return new ResumoFotografo(fotografoId, lotes.size(), totalFotos.get(),
                                   totalVendas.get(), receita.get(), saldo.get());
    }
}

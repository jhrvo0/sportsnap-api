package com.sportsnap.marketplace.infraestrutura.seed;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;

@Component
@Order(1)
@ConditionalOnProperty(name = "sportsnap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private final FotografoRepositorio fotografoRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final FotoServico fotoServico;

    public DadosIniciais(FotografoRepositorio fotografoRepositorio,
                          LoteRepositorio loteRepositorio,
                          FotoServico fotoServico) {
        this.fotografoRepositorio = fotografoRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.fotoServico = fotoServico;
    }

    @Override
    public void run(String... args) {
        if (!fotografoRepositorio.listarTodos().isEmpty()) return;

        var pedro = fotografoRepositorio.salvar(new Fotografo("Pedro Lente", new Email("pedro@photo.com")));
        var carla = fotografoRepositorio.salvar(new Fotografo("Carla Foco",  new Email("carla@photo.com")));
        var bruno = fotografoRepositorio.salvar(new Fotografo("Bruno Click", new Email("bruno@photo.com")));

        var lote1 = loteRepositorio.salvar(new Lote(
            pedro.getId(), new SessaoId(1), new SpotId(1), "Surf Maracaipe - manha"));
        var lote2 = loteRepositorio.salvar(new Lote(
            carla.getId(), new SessaoId(2), new SpotId(2), "Corrida Ibirapuera - tarde"));
        var lote3 = loteRepositorio.salvar(new Lote(
            bruno.getId(), new SessaoId(3), new SpotId(3), "Skate Rezende - noite"));

        fotoServico.uploadEmLote(lote1.getId(), List.of(
            "/fotos/surf_001.jpg",
            "/fotos/surf_002.jpg",
            "/fotos/surf_003.jpg"));

        fotoServico.uploadEmLote(lote2.getId(), List.of(
            "/fotos/corrida_001.jpg",
            "/fotos/corrida_002.jpg",
            "/fotos/corrida_003.jpg",
            "/fotos/corrida_004.jpg"));

        fotoServico.uploadEmLote(lote3.getId(), List.of(
            "/fotos/skate_001.jpg",
            "/fotos/skate_002.jpg"));
    }
}

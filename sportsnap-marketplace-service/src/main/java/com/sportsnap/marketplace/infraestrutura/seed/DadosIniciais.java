package com.sportsnap.marketplace.infraestrutura.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;

@Component
@Order(1)
@ConditionalOnProperty(name = "sportsnap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private final FotografoRepositorio fotografoRepositorio;

    public DadosIniciais(FotografoRepositorio fotografoRepositorio) {
        this.fotografoRepositorio = fotografoRepositorio;
    }

    @Override
    public void run(String... args) {
        if (!fotografoRepositorio.listarTodos().isEmpty()) return;
        fotografoRepositorio.salvar(new Fotografo("Pedro Lente", new Email("pedro@photo.com")));
        fotografoRepositorio.salvar(new Fotografo("Carla Foco",  new Email("carla@photo.com")));
        fotografoRepositorio.salvar(new Fotografo("Bruno Click", new Email("bruno@photo.com")));
    }
}

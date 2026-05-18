package com.sportsnap.gamification.infraestrutura.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.gamification.dominio.atleta.Atleta;
import com.sportsnap.gamification.dominio.atleta.AtletaRepositorio;
import com.sportsnap.gamification.dominio.atleta.Email;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;

@Component
@Order(1)
@ConditionalOnProperty(name = "sportsnap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private final AtletaRepositorio atletaRepositorio;
    private final CartaOficialRepositorio cartaRepositorio;
    private final StatusPotencialRepositorio statusRepositorio;
    private final LicencaRepositorio licencaRepositorio;

    public DadosIniciais(AtletaRepositorio atletaRepositorio,
                          CartaOficialRepositorio cartaRepositorio,
                          StatusPotencialRepositorio statusRepositorio,
                          LicencaRepositorio licencaRepositorio) {
        this.atletaRepositorio = atletaRepositorio;
        this.cartaRepositorio = cartaRepositorio;
        this.statusRepositorio = statusRepositorio;
        this.licencaRepositorio = licencaRepositorio;
    }

    @Override
    public void run(String... args) {
        if (!atletaRepositorio.listarTodos().isEmpty()) return;

        var maria = atletaRepositorio.salvar(new Atleta("Maria Atleta", new Email("maria@email.com")));
        var joao  = atletaRepositorio.salvar(new Atleta("Joao Silva",   new Email("joao@email.com")));
        var ana   = atletaRepositorio.salvar(new Atleta("Ana Costa",    new Email("ana@email.com")));

        cartaRepositorio.salvar(new CartaOficial(maria.getId(), List.of(
            new AtributoEsportivo("Velocidade",  78, 1.0, "CORRIDA"),
            new AtributoEsportivo("Resistencia", 82, 1.0, "CORRIDA"),
            new AtributoEsportivo("Tecnica",     74, 1.0, "CORRIDA")
        ), 78.0, LocalDateTime.now().minusDays(2)));

        cartaRepositorio.salvar(new CartaOficial(joao.getId(), List.of(
            new AtributoEsportivo("Forca",     85, 1.0, "MUSCULACAO"),
            new AtributoEsportivo("Explosao",  80, 1.0, "MUSCULACAO"),
            new AtributoEsportivo("Tecnica",   72, 1.0, "MUSCULACAO")
        ), 79.0, LocalDateTime.now().minusDays(5)));

        cartaRepositorio.salvar(new CartaOficial(ana.getId(), List.of(
            new AtributoEsportivo("Equilibrio", 88, 1.0, "SURF"),
            new AtributoEsportivo("Agilidade",  76, 1.0, "SURF"),
            new AtributoEsportivo("Tecnica",    70, 1.0, "SURF")
        )));

        statusRepositorio.salvar(new StatusPotencial(maria.getId(), 35.0, 4, LocalDateTime.now().minusHours(6)));
        statusRepositorio.salvar(new StatusPotencial(joao.getId(),  20.0, 2, LocalDateTime.now().minusHours(20)));
        statusRepositorio.salvar(new StatusPotencial(ana.getId(),   12.0, 1, LocalDateTime.now().minusHours(36)));

        licencaRepositorio.registrar(new Licenca(maria.getId(), LocalDateTime.now().minusHours(1)));
        licencaRepositorio.registrar(new Licenca(joao.getId(),  LocalDateTime.now().minusDays(1)));
    }
}

package com.sportsnap.session.dominio.sessao;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.infraestrutura.memoria.SessaoRepositorioMemoria;
import com.sportsnap.session.infraestrutura.memoria.SpotRepositorioMemoria;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessaoServicoTest {

    private final SessaoRepositorioMemoria sessoes = new SessaoRepositorioMemoria();
    private final SpotRepositorioMemoria spots = new SpotRepositorioMemoria();
    private SessaoServico servico;
    private Spot spot;

    @BeforeEach
    void setUp() {
        servico = new SessaoServico(sessoes, spots);
        spot = spots.salvar(new Spot("Arena", new Coordenada(-8.0, -34.0), "Teste"));
    }

    @Test
    void cadastrarSessaoComInicioNoPassadoERejeitado() {
        var erro = assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(
            spot.getId(),
            new Periodo(LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1)),
            "Treino retroativo"
        ));

        assertTrue(erro.getMessage().contains("passado"));
    }

    @Test
    void editarSessaoEncerradaERejeitado() {
        var encerrada = sessoes.salvar(new Sessao(
            new SessaoId(1),
            spot.getId(),
            new Periodo(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1)),
            "Treino encerrado",
            false
        ));

        var erro = assertThrows(IllegalStateException.class, () -> servico.atualizar(
            encerrada.getId(),
            spot.getId(),
            new Periodo(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
            "Alterada"
        ));

        assertTrue(erro.getMessage().contains("encerrada"));
    }

    @Test
    void removerSoPermiteSessaoEncerrada() {
        var futura = sessoes.salvar(new Sessao(
            new SpotId(1),
            new Periodo(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
            "Treino futuro"
        ));

        assertThrows(IllegalStateException.class, () -> servico.remover(futura.getId()));
    }
}

package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.aplicacao.atleta.AtletaResumo;
import com.sportsnap.gamification.aplicacao.atleta.AtletaServicoAplicacao;
import com.sportsnap.gamification.aplicacao.ranking.CartaResumo;
import com.sportsnap.gamification.aplicacao.ranking.RankingServicoAplicacao;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.atleta.AtletaServico;
import com.sportsnap.gamification.dominio.atleta.Email;
import com.sportsnap.gamification.dominio.ranking.RankingServico;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico;

@RestController
@RequestMapping("/api/atletas")
public class AtletaControlador {

    @Autowired private AtletaServico atletaServico;
    @Autowired private AtletaServicoAplicacao atletaServicoAplicacao;
    @Autowired private SincronizacaoServico sincronizacaoServico;

    @GetMapping
    public List<AtletaResumo> listar() {
        return atletaServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public void cadastrar(@RequestBody AtletaDto dto) {
        atletaServico.cadastrar(dto.nome, new Email(dto.email));
    }

    @PostMapping("/{id}/sincronizar")
    public void sincronizar(@PathVariable int id) {
        sincronizacaoServico.sincronizar(new AtletaId(id));
    }

    @PostMapping("/{id}/checkin-registrado")
    public void registrarCheckIn(@PathVariable int id, @RequestBody CheckInNotificacaoDto dto) {
        // Recebe notificacao de check-in do session-service para futura acumulacao de XP
        System.out.println("Check-in registrado: atleta=" + id + ", sessao=" + dto.sessaoId());
    }

    public static class AtletaDto {
        public String nome;
        public String email;
    }

    public record CheckInNotificacaoDto(int sessaoId) {}
}

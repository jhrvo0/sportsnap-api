package com.sportsnap.session.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.session.aplicacao.checkin.CheckInLoteServicoAplicacao;
import com.sportsnap.session.aplicacao.checkin.CheckInLoteServicoAplicacao.CheckInRequest;
import com.sportsnap.session.aplicacao.checkin.CheckInLoteServicoAplicacao.CheckInResultado;
import com.sportsnap.session.aplicacao.sessao.SessaoResumo;
import com.sportsnap.session.aplicacao.sessao.SessaoServicoAplicacao;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.infraestrutura.cliente.GamificationCliente;

@RestController
@RequestMapping("/api/sessoes")
public class SessaoControlador {

    @Autowired private SessaoServico sessaoServico;
    @Autowired private SessaoServicoAplicacao sessaoServicoAplicacao;
    @Autowired private CheckInLoteServicoAplicacao checkInLoteServico;
    @Autowired private GamificationCliente gamificationCliente;

    @GetMapping
    public List<SessaoResumo> listar() {
        return sessaoServicoAplicacao.pesquisarResumos();
    }

    @GetMapping("/{id}")
    public SessaoResumo buscarPorId(@PathVariable int id) {
        return sessaoServicoAplicacao.buscarResumo(new SessaoId(id));
    }

    @PostMapping
    public void criar(@RequestBody SessaoDto dto) {
        var periodo = new Periodo(dto.inicio, dto.fim);
        sessaoServico.cadastrar(new SpotId(dto.spotId), periodo, dto.descricao);
    }

    @PostMapping("/checkins/lote")
    public List<CheckInResultado> realizarCheckInsEmLote(@RequestBody List<CheckInRequest> requests) {
        var resultados = checkInLoteServico.realizarEmLote(requests);
        resultados.stream()
            .filter(CheckInResultado::sucesso)
            .forEach(r -> gamificationCliente.notificarCheckIn(r.atletaId(), r.checkInId()));
        return resultados;
    }

    public static class SessaoDto {
        public int spotId;
        public LocalDateTime inicio;
        public LocalDateTime fim;
        public String descricao;
    }
}

package com.sportsnap.session.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sportsnap.session.aplicacao.sessao.SessaoResumo;
import com.sportsnap.session.aplicacao.sessao.SessaoServicoAplicacao;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.SpotId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sessoes")
public class SessaoControlador {

    @Autowired private SessaoServico sessaoServico;
    @Autowired private SessaoServicoAplicacao sessaoServicoAplicacao;

    @GetMapping
    public List<SessaoResumo> listar() {
        return sessaoServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public void criar(@RequestBody SessaoDto dto) {
        var periodo = new Periodo(dto.inicio, dto.fim);
        sessaoServico.cadastrar(new SpotId(dto.spotId), periodo, dto.descricao);
    }

    @PutMapping("/{id}")
    public void atualizar(@PathVariable int id, @RequestBody SessaoDto dto) {
        var periodo = new Periodo(dto.inicio, dto.fim);
        sessaoServico.atualizar(new SessaoId(id), new SpotId(dto.spotId), periodo, dto.descricao);
    }

    @PostMapping("/{id}/cancelar")
    public void cancelar(@PathVariable int id) {
        sessaoServico.cancelar(new SessaoId(id));
    }

    public static class SessaoDto {
        public int spotId;
        public LocalDateTime inicio;
        public LocalDateTime fim;
        public String descricao;
    }
}

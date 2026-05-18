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

import com.sportsnap.session.aplicacao.sessao.SessaoResumo;
import com.sportsnap.session.aplicacao.sessao.SessaoServicoAplicacao;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.SpotId;

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

    public static class SessaoDto {
        public int spotId;
        public LocalDateTime inicio;
        public LocalDateTime fim;
        public String descricao;
    }
}

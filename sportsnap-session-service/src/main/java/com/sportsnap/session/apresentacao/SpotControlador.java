package com.sportsnap.session.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.session.aplicacao.spot.SpotResumo;
import com.sportsnap.session.aplicacao.spot.SpotServicoAplicacao;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.SpotServico;

@RestController
@RequestMapping("/api/spots")
public class SpotControlador {

    @Autowired private SpotServico spotServico;
    @Autowired private SpotServicoAplicacao spotServicoAplicacao;

    @GetMapping
    public List<SpotResumo> listar() {
        return spotServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public void criar(@RequestBody SpotDto dto) {
        spotServico.cadastrar(dto.nome, new Coordenada(dto.latitude, dto.longitude), dto.descricao);
    }

    public static class SpotDto {
        public String nome;
        public double latitude;
        public double longitude;
        public String descricao;
    }
}

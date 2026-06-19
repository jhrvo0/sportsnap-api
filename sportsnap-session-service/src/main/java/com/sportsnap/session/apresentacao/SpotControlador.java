package com.sportsnap.session.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sportsnap.session.aplicacao.spot.SpotResumo;
import com.sportsnap.session.aplicacao.spot.SpotServicoAplicacao;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.dominio.spot.SpotServico;

@CrossOrigin(origins = "*")
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

    @PutMapping("/{id}")
    public void atualizar(@PathVariable int id, @RequestBody SpotDto dto) {
        spotServico.atualizar(new SpotId(id), dto.nome, new Coordenada(dto.latitude, dto.longitude), dto.descricao);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable int id) {
        spotServico.remover(new SpotId(id));
    }

    public static class SpotDto {
        public String nome;
        public double latitude;
        public double longitude;
        public String descricao;
    }
}

package com.sportsnap.marketplace.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.marketplace.aplicacao.fotografo.FotografoResumo;
import com.sportsnap.marketplace.aplicacao.fotografo.FotografoServicoAplicacao;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;

@RestController
@RequestMapping("/api/fotografos")
public class FotografoControlador {

    @Autowired private FotografoServico fotografoServico;
    @Autowired private FotografoServicoAplicacao fotografoServicoAplicacao;

    @GetMapping
    public List<FotografoResumo> listar() {
        return fotografoServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public void cadastrar(@RequestBody FotografoDto dto) {
        fotografoServico.cadastrar(dto.nome, new Email(dto.email));
    }

    public static class FotografoDto {
        public String nome;
        public String email;
    }
}

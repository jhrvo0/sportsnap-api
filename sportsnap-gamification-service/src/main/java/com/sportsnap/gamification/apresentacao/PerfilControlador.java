package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.aplicacao.perfil.PerfilResumo;
import com.sportsnap.gamification.aplicacao.perfil.PerfilServicoAplicacao;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.SugestaoConexao;
import com.sportsnap.gamification.dominio.perfil.Perfil;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilServico;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;

@RestController
@RequestMapping("/api/perfis")
public class PerfilControlador {

    @Autowired private PerfilServico perfilServico;
    @Autowired private PerfilServicoAplicacao perfilServicoAplicacao;
    @Autowired private ConexaoServico conexaoServico;

    @GetMapping
    public List<PerfilResumo> listar() {
        return perfilServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public Perfil criar(@RequestBody PerfilDto dto) {
        return perfilServico.criar(new AtletaId(dto.usuarioId), dto.nomeExibicao,
            TipoConta.valueOf(dto.tipoConta));
    }

    @GetMapping("/{id}")
    public Perfil obter(@PathVariable int id) {
        return perfilServico.obter(new PerfilId(id));
    }

    @PutMapping("/{id}")
    public Perfil editar(@PathVariable int id, @RequestBody PerfilEdicaoDto dto) {
        return perfilServico.editar(new PerfilId(id), new AtletaId(dto.solicitanteId),
            dto.nomeExibicao, dto.bio, dto.esporte, dto.localidade,
            dto.visibilidade != null ? Visibilidade.valueOf(dto.visibilidade) : null);
    }

    @GetMapping("/usuario/{usuarioId}")
    public Perfil obterPorUsuario(@PathVariable int usuarioId) {
        return perfilServico.obterPorUsuario(new AtletaId(usuarioId));
    }

    @GetMapping("/{id}/sugestoes")
    public List<SugestaoConexao> sugestoes(@PathVariable int id) {
        return conexaoServico.sugerirConexoes(new PerfilId(id), 10);
    }

    public static class PerfilDto {
        public int    usuarioId;
        public String nomeExibicao;
        public String tipoConta;
    }

    public static class PerfilEdicaoDto {
        public int    solicitanteId;
        public String nomeExibicao;
        public String bio;
        public String esporte;
        public String localidade;
        public String visibilidade;
    }
}

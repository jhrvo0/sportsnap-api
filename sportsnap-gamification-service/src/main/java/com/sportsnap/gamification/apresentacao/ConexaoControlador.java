package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.conexao.Conexao;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.PedidoConexao;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoId;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@RestController
@RequestMapping("/api/conexoes")
public class ConexaoControlador {

    @Autowired private ConexaoServico conexaoServico;
    @Autowired private ConexaoRepositorio conexaoRepositorio;
    @Autowired private PedidoConexaoRepositorio pedidoRepositorio;

    @PostMapping("/seguir")
    public void seguir(@RequestBody ConexaoDto dto) {
        conexaoServico.seguir(new PerfilId(dto.seguidorId), new PerfilId(dto.seguidoId));
    }

    @DeleteMapping("/deixar-de-seguir")
    public void deixarDeSeguir(@RequestBody ConexaoDto dto) {
        conexaoServico.deixarDeSeguir(new PerfilId(dto.seguidorId), new PerfilId(dto.seguidoId));
    }

    @PostMapping("/pedidos/{id}/aprovar")
    public Conexao aprovarPedido(@PathVariable int id) {
        return conexaoServico.aprovar(new PedidoConexaoId(id));
    }

    @PostMapping("/pedidos/{id}/recusar")
    public void recusarPedido(@PathVariable int id) {
        conexaoServico.recusar(new PedidoConexaoId(id));
    }

    @DeleteMapping("/pedidos/{id}")
    public void cancelarPedido(@PathVariable int id, @RequestBody CancelamentoDto dto) {
        conexaoServico.cancelar(new PedidoConexaoId(id), new PerfilId(dto.canceladorId));
    }

    @PostMapping("/bloquear")
    public void bloquear(@RequestBody ConexaoDto dto) {
        conexaoServico.bloquear(new PerfilId(dto.seguidorId), new PerfilId(dto.seguidoId));
    }

    @GetMapping("/{perfilId}/seguidores")
    public List<Conexao> seguidores(@PathVariable int perfilId) {
        return conexaoRepositorio.listarSeguidores(new PerfilId(perfilId));
    }

    @GetMapping("/{perfilId}/seguindo")
    public List<Conexao> seguindo(@PathVariable int perfilId) {
        return conexaoRepositorio.listarSeguidos(new PerfilId(perfilId));
    }

    @GetMapping("/{perfilId}/pedidos-pendentes")
    public List<PedidoConexao> pedidosPendentes(@PathVariable int perfilId) {
        return pedidoRepositorio.listarPendentesPorAlvo(new PerfilId(perfilId));
    }

    public static class ConexaoDto {
        public int seguidorId;
        public int seguidoId;
    }

    public static class CancelamentoDto {
        public int canceladorId;
    }
}

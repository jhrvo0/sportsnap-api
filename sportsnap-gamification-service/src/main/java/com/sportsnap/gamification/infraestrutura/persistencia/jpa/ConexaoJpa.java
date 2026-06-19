package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.conexao.Conexao;
import com.sportsnap.gamification.dominio.conexao.ConexaoId;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.PedidoConexao;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoId;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.StatusPedido;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@Entity
@Table(name = "CONEXAO_SOCIAL")
class ConexaoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "SEGUIDOR_ID")
    int seguidorId;

    @Column(name = "SEGUIDO_ID")
    int seguidoId;

    @Column(name = "CRIADA_EM")
    LocalDateTime criadaEm;
}

interface ConexaoJpaRepository extends JpaRepository<ConexaoJpa, Integer> {
    List<ConexaoJpa> findBySeguidorId(int seguidorId);
    List<ConexaoJpa> findBySeguidoId(int seguidoId);
    Optional<ConexaoJpa> findBySeguidorIdAndSeguidoId(int seguidorId, int seguidoId);
}

@Entity
@Table(name = "PEDIDO_CONEXAO")
class PedidoConexaoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "SOLICITANTE_ID")
    int solicitanteId;

    @Column(name = "ALVO_ID")
    int alvoId;

    @Column(name = "CRIADO_EM")
    LocalDateTime criadoEm;

    String status;
}

interface PedidoConexaoJpaRepository extends JpaRepository<PedidoConexaoJpa, Integer> {
    List<PedidoConexaoJpa> findByAlvoIdAndStatus(int alvoId, String status);
    Optional<PedidoConexaoJpa> findBySolicitanteIdAndAlvoIdAndStatus(int sol, int alvo, String status);
    void deleteBySolicitanteIdAndAlvoId(int solicitanteId, int alvoId);
}

@Repository
class ConexaoRepositorioImpl implements ConexaoRepositorio {

    @Autowired ConexaoJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public Conexao salvar(Conexao conexao) {
        var jpa  = mapeador.paraJpa(conexao);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public void remover(ConexaoId id) {
        repositorio.deleteById(id.getId());
    }

    @Override
    public Optional<Conexao> obterPorPar(PerfilId seguidorId, PerfilId seguidoId) {
        return repositorio.findBySeguidorIdAndSeguidoId(seguidorId.getId(), seguidoId.getId())
            .map(mapeador::paraDominio);
    }

    @Override
    public List<Conexao> listarSeguidores(PerfilId seguidoId) {
        return repositorio.findBySeguidoId(seguidoId.getId()).stream()
            .map(mapeador::paraDominio).toList();
    }

    @Override
    public List<Conexao> listarSeguidos(PerfilId seguidorId) {
        return repositorio.findBySeguidorId(seguidorId.getId()).stream()
            .map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

@Repository
class PedidoConexaoRepositorioImpl implements PedidoConexaoRepositorio {

    @Autowired PedidoConexaoJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public PedidoConexao salvar(PedidoConexao pedido) {
        var jpa  = mapeador.paraJpa(pedido);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Override
    public void remover(PedidoConexaoId id) {
        repositorio.deleteById(id.getId());
    }

    @Override
    public Optional<PedidoConexao> obter(PedidoConexaoId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public Optional<PedidoConexao> obterPendentePorPar(PerfilId solicitanteId, PerfilId alvoId) {
        return repositorio.findBySolicitanteIdAndAlvoIdAndStatus(
                solicitanteId.getId(), alvoId.getId(), StatusPedido.PENDENTE.name())
            .map(mapeador::paraDominio);
    }

    @Override
    public List<PedidoConexao> listarPendentesPorAlvo(PerfilId alvoId) {
        return repositorio.findByAlvoIdAndStatus(alvoId.getId(), StatusPedido.PENDENTE.name())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void removerPorPar(PerfilId solicitanteId, PerfilId alvoId) {
        repositorio.deleteBySolicitanteIdAndAlvoId(solicitanteId.getId(), alvoId.getId());
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

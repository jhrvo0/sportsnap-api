package com.sportsnap.session.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.session.aplicacao.sessao.SessaoRepositorioAplicacao;
import com.sportsnap.session.aplicacao.sessao.SessaoResumo;
import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.spot.SpotId;

@Entity
@Table(name = "SESSAO")
class SessaoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int spotId;
    LocalDateTime periodoInicio;
    LocalDateTime periodoFim;
    String descricao;
    boolean cancelada;
}

interface SessaoJpaRepository extends JpaRepository<SessaoJpa, Integer> {
    List<SessaoJpa> findBySpotId(int spotId);
    List<SessaoJpa> findByCanceladaFalse();
    List<SessaoResumo> findSessaoResumoBy();
}

@Repository
class SessaoRepositorioImpl implements SessaoRepositorio, SessaoRepositorioAplicacao {

    @Autowired
    SessaoJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Sessao salvar(Sessao sessao) {
        var salva = repositorio.save(mapeador.paraJpa(sessao));
        return mapeador.paraDominio(salva);
    }

    @Override
    public Optional<Sessao> obter(SessaoId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Sessao> listarPorSpot(SpotId spotId) {
        return repositorio.findBySpotId(spotId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<Sessao> listarAtivas(LocalDateTime agora) {
        return repositorio.findAll().stream()
            .filter(s -> !s.cancelada && s.periodoInicio != null && s.periodoFim != null
                && !agora.isBefore(s.periodoInicio) && !agora.isAfter(s.periodoFim))
            .map(mapeador::paraDominio)
            .toList();
    }

    @Override
    public List<Sessao> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repositorio.findAll().stream()
            .filter(s -> s.periodoInicio != null && !s.periodoInicio.isAfter(fim)
                && s.periodoFim != null && !s.periodoFim.isBefore(inicio))
            .map(mapeador::paraDominio)
            .toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public List<SessaoResumo> pesquisarResumos() {
        return repositorio.findSessaoResumoBy();
    }
}

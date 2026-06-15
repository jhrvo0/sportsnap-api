package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRanking;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRankingRepositorio;

@Entity
@Table(name = "PONTUACAO_RANKING")
class PontuacaoRankingJpa {
    @Id
    @Column(name = "ATLETA_ID")
    int atletaId;
    double pr;
    LocalDateTime ultimaPartida;
}

interface PontuacaoRankingJpaRepository extends JpaRepository<PontuacaoRankingJpa, Integer> {
    List<PontuacaoRankingJpa> findAllByOrderByPrDesc();
}

@Repository
class PontuacaoRankingRepositorioImpl implements PontuacaoRankingRepositorio {

    @Autowired
    PontuacaoRankingJpaRepository repositorio;

    @Override
    public PontuacaoRanking salvar(PontuacaoRanking pontuacao) {
        return paraDominio(repositorio.save(paraJpa(pontuacao)));
    }

    @Override
    public Optional<PontuacaoRanking> obterPorAtleta(AtletaId atletaId) {
        return repositorio.findById(atletaId.getId()).map(this::paraDominio);
    }

    @Override
    public List<PontuacaoRanking> listarOrdenadasPorPr() {
        return repositorio.findAllByOrderByPrDesc().stream().map(this::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private PontuacaoRanking paraDominio(PontuacaoRankingJpa jpa) {
        return new PontuacaoRanking(new AtletaId(jpa.atletaId), jpa.pr, jpa.ultimaPartida);
    }

    private PontuacaoRankingJpa paraJpa(PontuacaoRanking dominio) {
        var jpa = new PontuacaoRankingJpa();
        jpa.atletaId = dominio.getAtletaId().getId();
        jpa.pr = dominio.getPr();
        jpa.ultimaPartida = dominio.getUltimaPartida();
        return jpa;
    }
}

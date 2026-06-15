package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.aplicacao.ranking.CartaResumo;
import com.sportsnap.gamification.aplicacao.ranking.RankingRepositorioAplicacao;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;

@Entity
@Table(name = "CARTA_OFICIAL")
class CartaOficialJpa {
    @Id
    @Column(name = "ATLETA_ID")
    int atletaId;

    double overall;
    LocalDateTime ultimaSincronizacao;

    @Column(name = "TIER")
    String tier;
    double saldoPontos;
    boolean arquivada;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ATRIBUTO_ESPORTIVO", joinColumns = @JoinColumn(name = "ATLETA_ID"))
    List<AtributoEsportivoJpa> atributos = new ArrayList<>();
}

@Embeddable
class AtributoEsportivoJpa {
    String nome;
    double valor;
    double peso;
    String tipoEsporte;
}

interface CartaOficialJpaRepository extends JpaRepository<CartaOficialJpa, Integer> {
    List<CartaResumo> findCartaResumoByUltimaSincronizacaoIsNotNullOrderByOverallDesc();
}

@Repository
class CartaOficialRepositorioImpl implements CartaOficialRepositorio, RankingRepositorioAplicacao {

    @Autowired
    CartaOficialJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public CartaOficial salvar(CartaOficial carta) {
        var jpa = mapeador.paraJpa(carta);
        var salva = repositorio.save(jpa);
        return mapeador.paraDominio(salva);
    }

    @Override
    public Optional<CartaOficial> obterPorAtleta(AtletaId atletaId) {
        return repositorio.findById(atletaId.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<CartaOficial> listarTodas() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<CartaOficial> listarSincronizadasOrdenadasPorOverall() {
        return repositorio.findAll().stream()
            .filter(c -> c.ultimaSincronizacao != null)
            .sorted((a, b) -> Double.compare(b.overall, a.overall))
            .map(mapeador::paraDominio)
            .toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public List<CartaResumo> pesquisarRankingGlobal() {
        return repositorio.findCartaResumoByUltimaSincronizacaoIsNotNullOrderByOverallDesc();
    }
}

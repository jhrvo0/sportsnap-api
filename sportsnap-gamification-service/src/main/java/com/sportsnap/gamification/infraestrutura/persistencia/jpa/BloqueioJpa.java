package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

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

import com.sportsnap.gamification.dominio.bloqueio.Bloqueio;
import com.sportsnap.gamification.dominio.bloqueio.BloqueioRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@Entity
@Table(name = "BLOQUEIO_SOCIAL")
class BloqueioJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "BLOQUEADOR_ID")
    int bloqueadorId;

    @Column(name = "BLOQUEADO_ID")
    int bloqueadoId;
}

interface BloqueioJpaRepository extends JpaRepository<BloqueioJpa, Integer> {
    Optional<BloqueioJpa> findByBloqueadorIdAndBloqueadoId(int bloqueadorId, int bloqueadoId);
    List<BloqueioJpa> findByBloqueadorIdOrBloqueadoId(int bloqueadorId, int bloqueadoId);
    void deleteByBloqueadorIdAndBloqueadoId(int bloqueadorId, int bloqueadoId);
    boolean existsByBloqueadorIdAndBloqueadoId(int bloqueadorId, int bloqueadoId);
}

@Repository
class BloqueioRepositorioImpl implements BloqueioRepositorio {

    @Autowired BloqueioJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public Bloqueio salvar(Bloqueio bloqueio) {
        var jpa  = mapeador.paraJpa(bloqueio);
        var salvo = repositorio.save(jpa);
        return mapeador.paraDominio(salvo);
    }

    @Transactional
    @Override
    public void remover(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        repositorio.deleteByBloqueadorIdAndBloqueadoId(bloqueadorId.getId(), bloqueadoId.getId());
    }

    @Override
    public Optional<Bloqueio> obter(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        return repositorio.findByBloqueadorIdAndBloqueadoId(bloqueadorId.getId(), bloqueadoId.getId())
            .map(mapeador::paraDominio);
    }

    @Override
    public boolean existeEntreAmbos(PerfilId a, PerfilId b) {
        return repositorio.existsByBloqueadorIdAndBloqueadoId(a.getId(), b.getId())
            || repositorio.existsByBloqueadorIdAndBloqueadoId(b.getId(), a.getId());
    }

    @Override
    public List<Bloqueio> listarEnvolvendo(PerfilId perfilId) {
        return repositorio.findByBloqueadorIdOrBloqueadoId(perfilId.getId(), perfilId.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

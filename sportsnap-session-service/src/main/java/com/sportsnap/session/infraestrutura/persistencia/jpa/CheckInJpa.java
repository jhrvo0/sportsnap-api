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

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.checkin.CheckInRepositorio;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;

@Entity
@Table(name = "CHECK_IN")
class CheckInJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int atletaId;
    int sessaoId;
    LocalDateTime horario;
    double latitude;
    double longitude;
    boolean cancelado;
    boolean atividadeRegistrada;
}

interface CheckInJpaRepository extends JpaRepository<CheckInJpa, Integer> {
    List<CheckInJpa> findByAtletaId(int atletaId);
    List<CheckInJpa> findBySessaoId(int sessaoId);
    Optional<CheckInJpa> findByAtletaIdAndSessaoId(int atletaId, int sessaoId);
}

@Repository
class CheckInRepositorioImpl implements CheckInRepositorio {

    @Autowired
    CheckInJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public CheckIn salvar(CheckIn checkIn) {
        var salvo = repositorio.save(mapeador.paraJpa(checkIn));
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<CheckIn> obter(CheckInId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<CheckIn> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaId(atletaId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<CheckIn> listarPorSessao(SessaoId sessaoId) {
        return repositorio.findBySessaoId(sessaoId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public Optional<CheckIn> obterPorAtletaESessao(AtletaId atletaId, SessaoId sessaoId) {
        return repositorio.findByAtletaIdAndSessaoId(atletaId.getId(), sessaoId.getId())
            .map(mapeador::paraDominio);
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

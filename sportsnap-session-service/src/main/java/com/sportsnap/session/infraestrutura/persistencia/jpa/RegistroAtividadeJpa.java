package com.sportsnap.session.infraestrutura.persistencia.jpa;

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

import com.sportsnap.session.dominio.atividade.Intensidade;
import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeId;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeRepositorio;
import com.sportsnap.session.dominio.checkin.CheckInId;

@Entity
@Table(name = "REGISTRO_ATIVIDADE")
class RegistroAtividadeJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int checkInId;
    double distancia;
    long duracaoSegundos;
    String intensidade;
    double xpCalculado;
}

interface RegistroAtividadeJpaRepository extends JpaRepository<RegistroAtividadeJpa, Integer> {
    List<RegistroAtividadeJpa> findByCheckInId(int checkInId);
}

@Repository
class RegistroAtividadeRepositorioImpl implements RegistroAtividadeRepositorio {

    @Autowired
    RegistroAtividadeJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public RegistroAtividade salvar(RegistroAtividade registro) {
        var salvo = repositorio.save(mapeador.paraJpa(registro));
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<RegistroAtividade> obter(RegistroAtividadeId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId) {
        return repositorio.findByCheckInId(checkInId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

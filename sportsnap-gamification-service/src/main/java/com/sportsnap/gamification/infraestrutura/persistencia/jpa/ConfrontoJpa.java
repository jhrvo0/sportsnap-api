package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.competicao.Confronto;
import com.sportsnap.gamification.dominio.competicao.ConfrontoRepositorio;

@Entity
@Table(name = "CONFRONTO")
class ConfrontoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    int vencedorId;
    int perdedorId;
    double prTransferida;
    LocalDateTime ocorridoEm;
    int temporadaId;
}

interface ConfrontoJpaRepository extends JpaRepository<ConfrontoJpa, Integer> {
    List<ConfrontoJpa> findByVencedorIdOrPerdedorIdOrderByOcorridoEmAsc(int vencedorId, int perdedorId);
}

@Repository
class ConfrontoRepositorioImpl implements ConfrontoRepositorio {

    @Autowired
    ConfrontoJpaRepository repositorio;

    @Override
    public Confronto salvar(Confronto confronto) {
        return paraDominio(repositorio.save(paraJpa(confronto)));
    }

    @Override
    public List<Confronto> listarPorAtleta(AtletaId atletaId) {
        int id = atletaId.getId();
        return repositorio.findByVencedorIdOrPerdedorIdOrderByOcorridoEmAsc(id, id).stream()
            .map(this::paraDominio)
            .toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    private Confronto paraDominio(ConfrontoJpa jpa) {
        return new Confronto(jpa.id, new AtletaId(jpa.vencedorId), new AtletaId(jpa.perdedorId),
            jpa.prTransferida, jpa.ocorridoEm, jpa.temporadaId);
    }

    private ConfrontoJpa paraJpa(Confronto dominio) {
        var jpa = new ConfrontoJpa();
        jpa.id = dominio.getId();
        jpa.vencedorId = dominio.getVencedorId().getId();
        jpa.perdedorId = dominio.getPerdedorId().getId();
        jpa.prTransferida = dominio.getPrTransferida();
        jpa.ocorridoEm = dominio.getOcorridoEm();
        jpa.temporadaId = dominio.getTemporadaId();
        return jpa;
    }
}

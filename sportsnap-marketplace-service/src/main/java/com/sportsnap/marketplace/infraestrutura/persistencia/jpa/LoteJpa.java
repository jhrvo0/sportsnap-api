package com.sportsnap.marketplace.infraestrutura.persistencia.jpa;

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

import com.sportsnap.marketplace.dominio.fotografo.FotografoId;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteId;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;

@Entity
@Table(name = "LOTE")
class LoteJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int fotografoId;
    int sessaoId;
    int spotId;
    String descricao;
    LocalDateTime criadoEm;
    boolean arquivado;
}

interface LoteJpaRepository extends JpaRepository<LoteJpa, Integer> {
    List<LoteJpa> findByFotografoId(int fotografoId);
}

@Repository
class LoteRepositorioImpl implements LoteRepositorio {

    @Autowired
    LoteJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Lote salvar(Lote lote) {
        var salvo = repositorio.save(mapeador.paraJpa(lote));
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<Lote> obter(LoteId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Lote> listarPorFotografo(FotografoId fotografoId) {
        return repositorio.findByFotografoId(fotografoId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

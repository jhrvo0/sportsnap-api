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

import com.sportsnap.marketplace.aplicacao.foto.FotoRepositorioAplicacao;
import com.sportsnap.marketplace.aplicacao.foto.FotoResumo;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.lote.LoteId;

@Entity
@Table(name = "FOTO")
class FotoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int loteId;
    String urlPreview;
    String urlOriginal;
    LocalDateTime exifTimestamp;
    String exifDetalhes;
    boolean licenciada;
    boolean removida;
}

interface FotoJpaRepository extends JpaRepository<FotoJpa, Integer> {
    List<FotoJpa> findByLoteIdAndRemovidaFalse(int loteId);
    List<FotoResumo> findFotoResumoBy();
}

@Repository
class FotoRepositorioImpl implements FotoRepositorio, FotoRepositorioAplicacao {

    @Autowired
    FotoJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public Foto salvar(Foto foto) {
        var salva = repositorio.save(mapeador.paraJpa(foto));
        return mapeador.paraDominio(salva);
    }

    @Override
    public Optional<Foto> obter(FotoId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Foto> listarPorLote(LoteId loteId) {
        return repositorio.findByLoteIdAndRemovidaFalse(loteId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<Foto> listarTodas() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }

    @Override
    public List<FotoResumo> pesquisarResumos() {
        return repositorio.findFotoResumoBy();
    }
}

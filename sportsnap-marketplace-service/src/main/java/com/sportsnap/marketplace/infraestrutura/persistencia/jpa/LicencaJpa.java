package com.sportsnap.marketplace.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
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

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.licenca.Dinheiro;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaId;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitFinanceiro;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;

@Entity
@Table(name = "LICENCA_IMAGEM")
class LicencaDeImagemJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int atletaId;
    int fotoId;
    BigDecimal preco;
    LocalDateTime adquiridaEm;
    boolean cancelada;
}

interface LicencaDeImagemJpaRepository extends JpaRepository<LicencaDeImagemJpa, Integer> {
    List<LicencaDeImagemJpa> findByAtletaId(int atletaId);
    List<LicencaDeImagemJpa> findByFotoId(int fotoId);
}

@Repository
class LicencaRepositorioImpl implements LicencaRepositorio {

    @Autowired
    LicencaDeImagemJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public LicencaDeImagem salvar(LicencaDeImagem licenca) {
        var salva = repositorio.save(mapeador.paraJpa(licenca));
        return mapeador.paraDominio(salva);
    }

    @Override
    public Optional<LicencaDeImagem> obter(LicencaId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<LicencaDeImagem> listarPorAtleta(AtletaId atletaId) {
        return repositorio.findByAtletaId(atletaId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<LicencaDeImagem> listarPorFoto(FotoId fotoId) {
        return repositorio.findByFotoId(fotoId.getId()).stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<LicencaDeImagem> listarTodas() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

@Entity
@Table(name = "SPLIT_FINANCEIRO")
class SplitFinanceiroJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int licencaId;
    BigDecimal valorFotografo;
    BigDecimal taxaPlataforma;
    LocalDateTime processadoEm;
}

interface SplitFinanceiroJpaRepository extends JpaRepository<SplitFinanceiroJpa, Integer> {
    Optional<SplitFinanceiroJpa> findByLicencaId(int licencaId);
    List<SplitFinanceiroJpa> findByLicencaIdIn(List<Integer> licencaIds);
}

@Repository
class SplitRepositorioImpl implements SplitRepositorio {

    @Autowired
    SplitFinanceiroJpaRepository repositorio;

    @Autowired
    JpaMapeador mapeador;

    @Override
    public SplitFinanceiro salvar(SplitFinanceiro split) {
        var salvo = repositorio.save(mapeador.paraJpa(split));
        return mapeador.paraDominio(salvo);
    }

    @Override
    public Optional<SplitFinanceiro> obterPorLicenca(LicencaId licencaId) {
        return repositorio.findByLicencaId(licencaId.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<SplitFinanceiro> listarTodos() {
        return repositorio.findAll().stream().map(mapeador::paraDominio).toList();
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

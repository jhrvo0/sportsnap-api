package com.sportsnap.gamification.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sportsnap.gamification.dominio.mensagem.Mensagem;
import com.sportsnap.gamification.dominio.mensagem.MensagemId;
import com.sportsnap.gamification.dominio.mensagem.MensagemRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

@Entity
@Table(name = "MENSAGEM_DIRETA")
class MensagemJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "REMETENTE_ID")
    int remetenteId;

    @Column(name = "DESTINATARIO_ID")
    int destinatarioId;

    @Column(length = 1000)
    String conteudo;

    boolean lida;

    @Column(name = "CRIADA_EM")
    LocalDateTime criadaEm;
}

interface MensagemJpaRepository extends JpaRepository<MensagemJpa, Integer> {
    @Query("SELECT m FROM MensagemJpa m WHERE " +
           "(m.remetenteId = :a AND m.destinatarioId = :b) OR " +
           "(m.remetenteId = :b AND m.destinatarioId = :a) " +
           "ORDER BY m.criadaEm ASC")
    List<MensagemJpa> findConversa(@Param("a") int a, @Param("b") int b);

    @Query("SELECT m FROM MensagemJpa m WHERE " +
           "m.remetenteId = :id OR m.destinatarioId = :id " +
           "ORDER BY m.criadaEm DESC")
    List<MensagemJpa> findEnvolvendo(@Param("id") int id);

    int countByDestinatarioIdAndLida(int destinatarioId, boolean lida);
}

@Repository
class MensagemRepositorioImpl implements MensagemRepositorio {

    @Autowired MensagemJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Override
    public Mensagem salvar(Mensagem mensagem) {
        return mapeador.paraDominio(repositorio.save(mapeador.paraJpa(mensagem)));
    }

    @Override
    public Optional<Mensagem> obter(MensagemId id) {
        return repositorio.findById(id.getId()).map(mapeador::paraDominio);
    }

    @Override
    public List<Mensagem> listarConversa(PerfilId perfilId1, PerfilId perfilId2) {
        return repositorio.findConversa(perfilId1.getId(), perfilId2.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public List<Mensagem> listarEnvolvendo(PerfilId perfilId) {
        return repositorio.findEnvolvendo(perfilId.getId())
            .stream().map(mapeador::paraDominio).toList();
    }

    @Override
    public int contarNaoLidas(PerfilId destinatarioId) {
        return repositorio.countByDestinatarioIdAndLida(destinatarioId.getId(), false);
    }

    @Transactional
    @Override
    public void limpar() {
        repositorio.deleteAll();
    }
}

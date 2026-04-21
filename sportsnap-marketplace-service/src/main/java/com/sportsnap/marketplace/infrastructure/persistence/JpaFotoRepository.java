package com.sportsnap.marketplace.infrastructure.persistence;

import com.sportsnap.marketplace.domain.entities.Foto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JpaFotoRepository extends JpaRepository<Foto, Long> {

    List<Foto> findByLoteId(Long loteId);

    /**
     * JPA Lock: Pessimistic Write Lock para cenario de compra concorrente.
     *
     * Cenario: Dois atletas tentam comprar a licenca da mesma foto ao mesmo tempo.
     * Sem lock, ambos poderiam ler a foto, verificar disponibilidade e criar licencas
     * duplicadas. Com PESSIMISTIC_WRITE, o segundo atleta espera o primeiro concluir.
     *
     * Conflito de escrita concorrente documentado conforme requisito do PDF.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Foto f WHERE f.id = :id")
    Optional<Foto> findByIdComLock(Long id);
}

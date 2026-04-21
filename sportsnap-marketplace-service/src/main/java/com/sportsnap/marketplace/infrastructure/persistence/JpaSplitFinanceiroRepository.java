package com.sportsnap.marketplace.infrastructure.persistence;

import com.sportsnap.marketplace.domain.entities.SplitFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSplitFinanceiroRepository extends JpaRepository<SplitFinanceiro, Long> {

    Optional<SplitFinanceiro> findByLicencaDeImagemId(Long licencaId);
}

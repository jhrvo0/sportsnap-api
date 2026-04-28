package com.sportsnap.marketplace.domain.repositories;

import com.sportsnap.marketplace.domain.entities.SplitFinanceiro;
import java.util.Optional;

public interface SplitFinanceiroRepository {

    SplitFinanceiro save(SplitFinanceiro split);

    Optional<SplitFinanceiro> findById(Long id);

    Optional<SplitFinanceiro> findByLicencaDeImagemId(Long licencaId);
}

package com.sportsnap.marketplace.apresentacao;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.marketplace.dominio.dashboard.DashboardServico;
import com.sportsnap.marketplace.dominio.fotografo.FotografoId;

@RestController
@RequestMapping("/api/fotografos")
public class DashboardControlador {

    @Autowired private DashboardServico dashboardServico;

    @GetMapping("/{id}/dashboard")
    public DashboardDto consultarDashboard(@PathVariable int id) {
        var resumo = dashboardServico.consultarResumo(new FotografoId(id));
        return new DashboardDto(
            resumo.getFotografoId().getId(),
            resumo.getTotalLotes(),
            resumo.getTotalFotos(),
            resumo.getTotalVendas(),
            resumo.getReceitaBruta().getValor(),
            resumo.getSaldoDisponivel().getValor(),
            resumo.getSaldoPendente().getValor()
        );
    }

    public record DashboardDto(
        int fotografoId,
        int totalLotes,
        int totalFotos,
        int totalVendas,
        BigDecimal receitaBruta,
        BigDecimal saldoDisponivel,
        BigDecimal saldoPendente
    ) {}
}

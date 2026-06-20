package com.sportsnap.marketplace.dominio.assinatura;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;

import org.junit.jupiter.api.Test;

class AssinaturaTest {

    @Test
    void criarAssinaturaComCotasCorretas() {
        var assinatura = new Assinatura(new AtletaId(1), 10);

        assertNotNull(assinatura.getId());
        assertEquals(AssinaturaStatus.ATIVA, assinatura.getStatus());
        assertEquals(10, assinatura.getSaldoCotas());
        assertTrue(assinatura.isAtiva());
    }

    @Test
    void consumirCotaFIFO() {
        var assinatura = new Assinatura(new AtletaId(1), 5);

        assinatura.consumirCota();

        assertEquals(4, assinatura.getSaldoCotas());
    }

    @Test
    void consumirSemCotasLancaErro() {
        var assinatura = new Assinatura(new AtletaId(1), 0);

        var erro = assertThrows(IllegalStateException.class, assinatura::consumirCota);
        assertTrue(erro.getMessage().contains("Saldo de cotas insuficiente"));
    }

    @Test
    void consumirEmAssinaturaInativa() {
        var assinatura = new Assinatura(new AtletaId(1), 5);
        assinatura.cancelarAutoRenovacao();
        assinatura.renovarCiclo(20, 10);

        var erro = assertThrows(IllegalStateException.class, assinatura::consumirCota);
        assertTrue(erro.getMessage().contains("nao esta ativa"));
    }

    @Test
    void restituirCota() {
        var assinatura = new Assinatura(new AtletaId(1), 5);
        assinatura.consumirCota();

        assinatura.restituirCota();

        assertEquals(5, assinatura.getSaldoCotas());
    }

    @Test
    void cancelarAutoRenovacao() {
        var assinatura = new Assinatura(new AtletaId(1), 5);

        assinatura.cancelarAutoRenovacao();

        assertEquals(AssinaturaStatus.CANCELADA_PENDENTE, assinatura.getStatus());
        assertTrue(assinatura.isAtiva());
        assertTrue(assinatura.possuiCota());
    }

    @Test
    void renovarCicloAtivoComRollover() {
        var assinatura = new Assinatura(new AtletaId(1), 5);

        assinatura.renovarCiclo(20, 10);

        assertEquals(AssinaturaStatus.ATIVA, assinatura.getStatus());
        assertTrue(assinatura.getSaldoCotas() >= 10);
    }

    @Test
    void renovarCicloCanceladoPendenteParaInativa() {
        var assinatura = new Assinatura(new AtletaId(1), 5);
        assinatura.cancelarAutoRenovacao();

        assinatura.renovarCiclo(20, 10);

        assertEquals(AssinaturaStatus.INATIVA, assinatura.getStatus());
        assertEquals(0, assinatura.getSaldoCotas());
    }

    @Test
    void renovarCicloInativoLancaErro() {
        var assinatura = new Assinatura(new AtletaId(1), 5);
        assinatura.cancelarAutoRenovacao();
        assinatura.renovarCiclo(20, 10);

        var erro = assertThrows(IllegalStateException.class, () -> assinatura.renovarCiclo(20, 10));
        assertTrue(erro.getMessage().contains("Assinatura inativa nao pode ser renovada"));
    }
}

package com.sportsnap.marketplace.dominio.assinatura;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.licenca.Dinheiro;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitFinanceiro;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.Foto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssinaturaServico {

    public static final int COTA_MENSAL = 10;
    public static final int LIMITE_ROLLOVER = 20;
    public static final Dinheiro VALOR_MENSALIDADE = Dinheiro.de("99.90");
    public static final BigDecimal PERCENTUAL_FOTOGRAFO = new BigDecimal("0.70");

    private final AssinaturaRepositorio assinaturaRepositorio;
    private final LicencaRepositorio licencaRepositorio;
    private final SplitRepositorio splitRepositorio;
    private final FotoRepositorio fotoRepositorio;

    public AssinaturaServico(AssinaturaRepositorio assinaturaRepositorio,
                             LicencaRepositorio licencaRepositorio,
                             SplitRepositorio splitRepositorio,
                             FotoRepositorio fotoRepositorio) {
        this.assinaturaRepositorio = assinaturaRepositorio;
        this.licencaRepositorio = licencaRepositorio;
        this.splitRepositorio = splitRepositorio;
        this.fotoRepositorio = fotoRepositorio;
    }

    public Assinatura assinar(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        
        Optional<Assinatura> existente = assinaturaRepositorio.obterPorAtleta(atletaId);
        if (existente.isPresent() && existente.get().isAtiva()) {
            throw new IllegalStateException("Atleta ja possui uma assinatura ativa.");
        }

        Assinatura assinatura = new Assinatura(atletaId, COTA_MENSAL);
        return assinaturaRepositorio.salvar(assinatura);
    }

    public void cancelar(AtletaId atletaId) {
        Assinatura assinatura = obterAtiva(atletaId);
        assinatura.cancelarAutoRenovacao();
        assinaturaRepositorio.salvar(assinatura);
    }

    public boolean possuiCotaDisponivel(AtletaId atletaId) {
        Optional<Assinatura> assinatura = assinaturaRepositorio.obterPorAtleta(atletaId);
        return assinatura.map(Assinatura::possuiCota).orElse(false);
    }

    public void debitarCota(AtletaId atletaId) {
        Assinatura assinatura = obterAtiva(atletaId);
        assinatura.consumirCota();
        assinaturaRepositorio.salvar(assinatura);
    }

    public void restituirCota(AtletaId atletaId) {
        Assinatura assinatura = obterAtiva(atletaId);
        assinatura.restituirCota();
        assinaturaRepositorio.salvar(assinatura);
    }

    public Assinatura obterAtiva(AtletaId atletaId) {
        return assinaturaRepositorio.obterPorAtleta(atletaId)
            .filter(Assinatura::isAtiva)
            .orElseThrow(() -> new IllegalStateException("Nenhuma assinatura ativa encontrada."));
    }

    public void fecharCicloEProcessarRateio(AtletaId atletaId) {
        Assinatura assinatura = obterAtiva(atletaId);

        // Busca todas as licenças do atleta no ciclo atual
        // Para simplificar, buscamos todas as adquiridas via cota que não têm split gerado ainda.
        // O certo seria buscar pela dataInicioCiclo e dataFimCiclo.
        List<LicencaDeImagem> licencasDoCiclo = licencaRepositorio.listarPorAtleta(atletaId).stream()
            .filter(l -> l.isAdquiridaViaCota() && !l.isCancelada())
            .filter(l -> splitRepositorio.obterPorLicenca(l.getId()).isEmpty())
            .collect(Collectors.toList());

        int totalDownloads = licencasDoCiclo.size();

        if (totalDownloads > 0) {
            // Valor total destinado aos fotógrafos no pool
            Dinheiro poolTotal = VALOR_MENSALIDADE.multiplicar(PERCENTUAL_FOTOGRAFO);

            // Rateio: divide o pool pelo número de downloads efetuados (1 cota gasta = 1 download)
            BigDecimal valorPorDownload = poolTotal.getValor().divide(new BigDecimal(totalDownloads), 2, RoundingMode.HALF_UP);
            Dinheiro repasseUnitario = new Dinheiro(valorPorDownload);

            // Para cada licença gerada via cota, criamos um SplitFinanceiro retroativo
            for (LicencaDeImagem licenca : licencasDoCiclo) {
                // A taxa de intermediação por licença para fins de registro será zero (pois o valor cheio foi cobrado na mensalidade)
                // ou proporcional. A regra diz: rateia os 70% entre as fotos. O restante (30%) fica pra plataforma.
                SplitFinanceiro split = new SplitFinanceiro(licenca.getId(), repasseUnitario, Dinheiro.ZERO);
                splitRepositorio.salvar(split);
            }
        }

        // Renova o ciclo fazendo o rollover
        assinatura.renovarCiclo(LIMITE_ROLLOVER, COTA_MENSAL);
        assinaturaRepositorio.salvar(assinatura);
    }
}

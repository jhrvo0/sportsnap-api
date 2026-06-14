package com.sportsnap.marketplace.dominio.licenca;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.evento.EventoBarramento;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VendaServico {

    public static final Dinheiro PRECO_PADRAO = Dinheiro.de("29.90");
    public static final BigDecimal PERCENTUAL_FOTOGRAFO = new BigDecimal("0.70");
    public static final BigDecimal PERCENTUAL_PLATAFORMA = new BigDecimal("0.30");

    private final LicencaRepositorio licencaRepositorio;
    private final SplitRepositorio splitRepositorio;
    private final FotoRepositorio fotoRepositorio;
    private final EventoBarramento barramento;

    public VendaServico(LicencaRepositorio licencaRepositorio,
                         SplitRepositorio splitRepositorio,
                         FotoRepositorio fotoRepositorio,
                         EventoBarramento barramento) {
        notNull(licencaRepositorio, "O repositorio de Licenca nao pode ser nulo");
        notNull(splitRepositorio, "O repositorio de Split nao pode ser nulo");
        notNull(fotoRepositorio, "O repositorio de Foto nao pode ser nulo");
        notNull(barramento, "O barramento de eventos nao pode ser nulo");
        this.licencaRepositorio = licencaRepositorio;
        this.splitRepositorio = splitRepositorio;
        this.fotoRepositorio = fotoRepositorio;
        this.barramento = barramento;
    }

    public synchronized LicencaDeImagem processarVenda(AtletaId atletaId, FotoId fotoId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(fotoId, "O id da Foto nao pode ser nulo");

        Foto foto = fotoRepositorio.obter(fotoId)
            .orElseThrow(() -> new IllegalArgumentException("Foto nao encontrada: " + fotoId));

        if (foto.isRemovida()) {
            throw new IllegalStateException("Foto removida nao pode ser adquirida");
        }

        boolean jaTemLicenca = licencaRepositorio.listarPorFoto(fotoId).stream()
            .anyMatch(l -> l.getAtletaId().equals(atletaId) && !l.isCancelada());
        if (jaTemLicenca) {
            throw new IllegalStateException("Atleta ja possui licenca ativa para esta Foto");
        }

        var licenca = new LicencaDeImagem(atletaId, fotoId, PRECO_PADRAO);
        var salva = licencaRepositorio.salvar(licenca);

        var valorFotografo = PRECO_PADRAO.multiplicar(PERCENTUAL_FOTOGRAFO);
        var taxaPlataforma = PRECO_PADRAO.multiplicar(PERCENTUAL_PLATAFORMA);
        var split = new SplitFinanceiro(salva.getId(), valorFotografo, taxaPlataforma);
        splitRepositorio.salvar(split);

        foto.marcarLicenciada();
        fotoRepositorio.salvar(foto);

        barramento.postar(new LicencaAdquiridaEvento(salva, split));
        return salva;
    }

    public LicencaDeImagem obter(LicencaId id) {
        notNull(id, "O id da Licenca nao pode ser nulo");
        return licencaRepositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Licenca nao encontrada: " + id));
    }

    public List<LicencaDeImagem> listarPorAtleta(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        return licencaRepositorio.listarPorAtleta(atletaId);
    }

    public SplitFinanceiro obterSplit(LicencaId licencaId) {
        notNull(licencaId, "O id da Licenca nao pode ser nulo");
        return splitRepositorio.obterPorLicenca(licencaId)
            .orElseThrow(() -> new IllegalArgumentException("Split nao encontrado para Licenca: " + licencaId));
    }

    public void cancelar(LicencaId id) {
        var licenca = obter(id);
        licenca.cancelar(LocalDateTime.now());
        licencaRepositorio.salvar(licenca);
        barramento.postar(new LicencaCanceladaEvento(licenca));
    }

    public Dinheiro calcularTotalGastoPeloAtleta(AtletaId atletaId) {
        return licencaRepositorio.listarPorAtleta(atletaId).stream()
            .filter(l -> !l.isCancelada())
            .map(LicencaDeImagem::getPreco)
            .reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public static class LicencaAdquiridaEvento {
        private final LicencaDeImagem licenca;
        private final SplitFinanceiro split;

        LicencaAdquiridaEvento(LicencaDeImagem licenca, SplitFinanceiro split) {
            this.licenca = licenca;
            this.split = split;
        }

        public LicencaDeImagem getLicenca() {
            return licenca;
        }

        public SplitFinanceiro getSplit() {
            return split;
        }
    }

    public static class LicencaCanceladaEvento {
        private final LicencaDeImagem licenca;

        LicencaCanceladaEvento(LicencaDeImagem licenca) {
            this.licenca = licenca;
        }

        public LicencaDeImagem getLicenca() {
            return licenca;
        }
    }
}

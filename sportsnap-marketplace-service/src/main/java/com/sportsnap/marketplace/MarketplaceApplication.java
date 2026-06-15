package com.sportsnap.marketplace;

import com.sportsnap.marketplace.aplicacao.foto.FotoRepositorioAplicacao;
import com.sportsnap.marketplace.aplicacao.foto.FotoServicoAplicacao;
import com.sportsnap.marketplace.aplicacao.fotografo.FotografoRepositorioAplicacao;
import com.sportsnap.marketplace.aplicacao.fotografo.FotografoServicoAplicacao;
import com.sportsnap.marketplace.dominio.dashboard.DashboardServico;
import com.sportsnap.marketplace.dominio.evento.EventoBarramento;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;
import com.sportsnap.marketplace.dominio.fotografo.FotografoServico;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;
import com.sportsnap.marketplace.dominio.lote.LoteServico;
import com.sportsnap.marketplace.dominio.sugestao.FavoritoRepositorio;
import com.sportsnap.marketplace.dominio.sugestao.MotorSugestaoServico;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }

    @Bean
    public FotografoServico fotografoServico(FotografoRepositorio repositorio) {
        return new FotografoServico(repositorio);
    }

    @Bean
    public FotografoServicoAplicacao fotografoServicoAplicacao(FotografoRepositorioAplicacao repositorio) {
        return new FotografoServicoAplicacao(repositorio);
    }

    @Bean
    public LoteServico loteServico(LoteRepositorio repositorio, FotografoRepositorio fotografoRepositorio) {
        return new LoteServico(repositorio, fotografoRepositorio);
    }

    @Bean
    public FotoServico fotoServico(FotoRepositorio repositorio, LoteRepositorio loteRepositorio) {
        return new FotoServico(repositorio, loteRepositorio);
    }

    @Bean
    public com.sportsnap.marketplace.dominio.assinatura.AssinaturaRepositorio assinaturaRepositorio() {
        return new com.sportsnap.marketplace.infraestrutura.memoria.AssinaturaRepositorioEmMemoria();
    }

    @Bean
    public com.sportsnap.marketplace.dominio.assinatura.AssinaturaServico assinaturaServico(
            com.sportsnap.marketplace.dominio.assinatura.AssinaturaRepositorio assinaturaRepositorio,
            LicencaRepositorio licencaRepositorio,
            SplitRepositorio splitRepositorio,
            FotoRepositorio fotoRepositorio) {
        return new com.sportsnap.marketplace.dominio.assinatura.AssinaturaServico(
            assinaturaRepositorio, licencaRepositorio, splitRepositorio, fotoRepositorio);
    }

    @Bean
    public VendaServico vendaServico(LicencaRepositorio licencaRepositorio,
                                     SplitRepositorio splitRepositorio,
                                     FotoRepositorio fotoRepositorio,
                                     EventoBarramento barramento,
                                     com.sportsnap.marketplace.dominio.assinatura.AssinaturaServico assinaturaServico) {
        return new VendaServico(licencaRepositorio, splitRepositorio, fotoRepositorio, barramento, assinaturaServico);
    }

    @Bean
    public FotoServicoAplicacao fotoServicoAplicacao(FotoRepositorioAplicacao repositorio) {
        return new FotoServicoAplicacao(repositorio);
    }

    @Bean
    public DashboardServico dashboardServico(FotografoRepositorio fotografoRepositorio,
                                              LoteRepositorio loteRepositorio,
                                              FotoRepositorio fotoRepositorio,
                                              LicencaRepositorio licencaRepositorio,
                                              SplitRepositorio splitRepositorio) {
        return new DashboardServico(fotografoRepositorio, loteRepositorio, fotoRepositorio,
                                    licencaRepositorio, splitRepositorio);
    }

    @Bean
    public MotorSugestaoServico motorSugestaoServico(FotoRepositorio fotoRepositorio,
                                                      LicencaRepositorio licencaRepositorio,
                                                      FavoritoRepositorio favoritoRepositorio,
                                                      LoteRepositorio loteRepositorio) {
        return new MotorSugestaoServico(fotoRepositorio, licencaRepositorio, favoritoRepositorio, loteRepositorio);
    }
}

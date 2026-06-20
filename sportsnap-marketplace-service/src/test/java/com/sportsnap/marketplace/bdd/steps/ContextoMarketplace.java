package com.sportsnap.marketplace.bdd.steps;

import com.sportsnap.marketplace.dominio.assinatura.AssinaturaRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;
import com.sportsnap.marketplace.dominio.licenca.SplitRepositorio;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;
import com.sportsnap.marketplace.dominio.sugestao.FavoritoRepositorio;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;

public class ContextoMarketplace {

    @Autowired private FotografoRepositorio fotografoRepositorio;
    @Autowired private LoteRepositorio loteRepositorio;
    @Autowired private FotoRepositorio fotoRepositorio;
    @Autowired private LicencaRepositorio licencaRepositorio;
    @Autowired private SplitRepositorio splitRepositorio;
    @Autowired private FavoritoRepositorio favoritoRepositorio;
    @Autowired private AssinaturaRepositorio assinaturaRepositorio;
    @Autowired private ColetorDeEventos coletorDeEventos;

    @Before
    public void limpar() {
        favoritoRepositorio.limpar();
        splitRepositorio.limpar();
        licencaRepositorio.limpar();
        fotoRepositorio.limpar();
        loteRepositorio.limpar();
        fotografoRepositorio.limpar();
        assinaturaRepositorio.limpar();
        coletorDeEventos.limpar();
    }

    @Dado("que o sistema de Marketplace esta limpo")
    public void sistemaLimpo() {
        // noop — @Before cuida da limpeza
    }
}

package com.sportsnap.marketplace.infraestrutura.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.Foto;
import com.sportsnap.marketplace.dominio.foto.FotoRepositorio;
import com.sportsnap.marketplace.dominio.foto.FotoServico;
import com.sportsnap.marketplace.dominio.foto.MetadadosExif;
import com.sportsnap.marketplace.dominio.fotografo.Email;
import com.sportsnap.marketplace.dominio.fotografo.Fotografo;
import com.sportsnap.marketplace.dominio.fotografo.FotografoRepositorio;
import com.sportsnap.marketplace.dominio.licenca.VendaServico;
import com.sportsnap.marketplace.dominio.lote.Lote;
import com.sportsnap.marketplace.dominio.lote.LoteRepositorio;
import com.sportsnap.marketplace.dominio.lote.LoteServico;
import com.sportsnap.marketplace.dominio.lote.SessaoId;
import com.sportsnap.marketplace.dominio.lote.SpotId;

@Component
@Order(1)
@ConditionalOnProperty(name = "sportsnap.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DadosIniciais implements CommandLineRunner {

    private final FotografoRepositorio fotografoRepositorio;
    private final LoteServico loteServico;
    private final FotoServico fotoServico;
    private final FotoRepositorio fotoRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final VendaServico vendaServico;

    public DadosIniciais(FotografoRepositorio fotografoRepositorio,
                         LoteServico loteServico,
                         FotoServico fotoServico,
                         FotoRepositorio fotoRepositorio,
                         LoteRepositorio loteRepositorio,
                         VendaServico vendaServico) {
        this.fotografoRepositorio = fotografoRepositorio;
        this.loteServico         = loteServico;
        this.fotoServico         = fotoServico;
        this.fotoRepositorio     = fotoRepositorio;
        this.loteRepositorio     = loteRepositorio;
        this.vendaServico        = vendaServico;
    }

    @Override
    public void run(String... args) {
        if (!fotografoRepositorio.listarTodos().isEmpty()) return;

        // ── Fotógrafos ────────────────────────────────────────────────────────
        var pedro = fotografoRepositorio.salvar(new Fotografo("Pedro Lente", new Email("pedro@sportsnap.com")));
        var carla = fotografoRepositorio.salvar(new Fotografo("Carla Foco",  new Email("carla@sportsnap.com")));
        var bruno = fotografoRepositorio.salvar(new Fotografo("Bruno Click", new Email("bruno@sportsnap.com")));

        // IDs de spots e sessões criados pelo session-service (gerados em ordem: 1,2,3,4,5)
        var spotMaracaipe  = new SpotId(1);
        var spotIbirapuera = new SpotId(2);
        var spotRezende    = new SpotId(3);
        var spotCampo      = new SpotId(4);
        var spotCiclovia   = new SpotId(5);
        var sessaoSurf     = new SessaoId(1);
        var sessaoCorrida  = new SessaoId(2);
        var sessaoSkate    = new SessaoId(3);

        // ── Lotes ─────────────────────────────────────────────────────────────
        var lote1 = loteServico.cadastrar(pedro.getId(), sessaoSurf,    spotMaracaipe,  "Surf em Maracaipe — Pedro Lente");
        var lote2 = loteServico.cadastrar(pedro.getId(), sessaoCorrida, spotIbirapuera, "Corrida no Ibirapuera — Pedro Lente");
        var lote3 = loteServico.cadastrar(carla.getId(), sessaoSkate,   spotRezende,    "Skate no Rezende — Carla Foco");
        var lote4 = loteServico.cadastrar(carla.getId(), sessaoCorrida, spotCiclovia,   "Pedal na Orla — Carla Foco");
        var lote5 = loteServico.cadastrar(bruno.getId(), sessaoSurf,    spotMaracaipe,  "Surf ao Pôr do Sol — Bruno Click");
        var lote6 = loteServico.cadastrar(bruno.getId(), sessaoCorrida, spotCampo,      "Futebol no Campo do Retiro — Bruno Click");

        // ── Fotos — criadas diretamente no repositório para mais controle ─────
        var agora = LocalDateTime.now();

        // Lote 1 — Surf Maracaipe (Pedro)
        var f1 = salvarFoto(lote1, "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=400", agora.minusDays(3), "Canon EOS R5 · 1/1000s · f/5.6 · ISO 200", new BigDecimal("49.90"));
        var f2 = salvarFoto(lote1, "https://images.unsplash.com/photo-1455729552865-3658a5d39692?w=400", agora.minusDays(3), "Canon EOS R5 · 1/800s · f/6.3 · ISO 250", new BigDecimal("39.90"));
        var f3 = salvarFoto(lote1, "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400", agora.minusDays(3), "Canon EOS R5 · 1/1250s · f/4.5 · ISO 160", new BigDecimal("59.90"));

        // Lote 2 — Corrida Ibirapuera (Pedro)
        var f4 = salvarFoto(lote2, "https://images.unsplash.com/photo-1461897104016-0b3b00cc81ee?w=400", agora.minusDays(5), "Sony A7III · 1/500s · f/4.0 · ISO 400", new BigDecimal("29.90"));
        var f5 = salvarFoto(lote2, "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=400", agora.minusDays(5), "Sony A7III · 1/640s · f/3.5 · ISO 320", new BigDecimal("34.90"));

        // Lote 3 — Skate Rezende (Carla)
        var f6 = salvarFoto(lote3, "https://images.unsplash.com/photo-1564415315949-7a0c4c73aab4?w=400", agora.minusDays(2), "Nikon Z6 · 1/1600s · f/4.0 · ISO 800", new BigDecimal("44.90"));
        var f7 = salvarFoto(lote3, "https://images.unsplash.com/photo-1547447134-cd3f5c716030?w=400", agora.minusDays(2), "Nikon Z6 · 1/2000s · f/3.5 · ISO 640", new BigDecimal("49.90"));
        var f8 = salvarFoto(lote3, "https://images.unsplash.com/photo-1541534741688-7078a4abb601?w=400", agora.minusDays(2), "Nikon Z6 · 1/1250s · f/4.5 · ISO 1000", new BigDecimal("39.90"));

        // Lote 4 — Ciclismo Orla (Carla)
        var f9  = salvarFoto(lote4, "https://images.unsplash.com/photo-1534787238916-9ba6764efd4f?w=400", agora.minusDays(4), "Fujifilm X-T4 · 1/1000s · f/5.6 · ISO 200", new BigDecimal("29.90"));
        var f10 = salvarFoto(lote4, "https://images.unsplash.com/photo-1541625602330-2277a4c46182?w=400", agora.minusDays(4), "Fujifilm X-T4 · 1/800s · f/6.3 · ISO 160", new BigDecimal("34.90"));

        // Lote 5 — Surf pôr do sol (Bruno)
        var f11 = salvarFoto(lote5, "https://images.unsplash.com/photo-1560272564-c83b66b1ad12?w=400", agora.minusDays(1), "Olympus OM-1 · 1/1600s · f/4.0 · ISO 400", new BigDecimal("69.90"));
        var f12 = salvarFoto(lote5, "https://images.unsplash.com/photo-1464746133101-a2c3f88e0dd9?w=400", agora.minusDays(1), "Olympus OM-1 · 1/1000s · f/5.0 · ISO 320", new BigDecimal("59.90"));

        // Lote 6 — Futebol (Bruno)
        var f13 = salvarFoto(lote6, "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=400", agora.minusDays(6), "Canon EOS R3 · 1/1000s · f/2.8 · ISO 800", new BigDecimal("29.90"));
        var f14 = salvarFoto(lote6, "https://images.unsplash.com/photo-1553778263-73a83bab9b0c?w=400", agora.minusDays(6), "Canon EOS R3 · 1/1250s · f/2.8 · ISO 640", new BigDecimal("34.90"));

        // Todas as fotos disponíveis para venda
        List.of(f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14).forEach(f -> {
            fotoServico.disponibilizar(f.getId());
        });

        // ── Licenças (atletas comprando fotos) ───────────────────────────────
        // atletaId 1 = Maria (corrida/surf)
        vendaServico.processarVenda(new AtletaId(1), f1.getId()); // foto surf
        vendaServico.processarVenda(new AtletaId(1), f4.getId()); // foto corrida
        vendaServico.processarVenda(new AtletaId(1), f5.getId()); // foto corrida

        // atletaId 2 = João (surf)
        vendaServico.processarVenda(new AtletaId(2), f2.getId()); // foto surf
        vendaServico.processarVenda(new AtletaId(2), f11.getId()); // surf pôr do sol

        // atletaId 3 = Ana (skate)
        vendaServico.processarVenda(new AtletaId(3), f6.getId()); // foto skate
        vendaServico.processarVenda(new AtletaId(3), f7.getId()); // foto skate

        // atletaId 4 = Lucas (futebol)
        vendaServico.processarVenda(new AtletaId(4), f13.getId()); // foto futebol
        vendaServico.processarVenda(new AtletaId(4), f14.getId()); // foto futebol

        // atletaId 5 = Beatriz (corrida)
        vendaServico.processarVenda(new AtletaId(5), f9.getId());  // ciclismo/corrida
        vendaServico.processarVenda(new AtletaId(5), f10.getId()); // ciclismo/corrida
    }

    private Foto salvarFoto(Lote lote, String urlPreview, LocalDateTime timestamp,
                             String detalhes, BigDecimal preco) {
        var foto = new Foto(lote.getId(), urlPreview, urlPreview.replace("w=400", "w=1200"),
            new MetadadosExif(timestamp, detalhes));
        foto.definirPreco(preco);
        return fotoRepositorio.salvar(foto);
    }
}

package com.sportsnap.marketplace.infrastructure.web;

import com.sportsnap.marketplace.domain.entities.*;
import com.sportsnap.marketplace.domain.usecases.ProcessarVendaFoto;
import com.sportsnap.marketplace.domain.usecases.UploadIndexacaoEmLote;
import com.sportsnap.marketplace.infrastructure.persistence.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final JpaFotografoRepository fotografoRepository;
    private final JpaLoteRepository loteRepository;
    private final JpaFotoRepository fotoRepository;
    private final JpaLicencaDeImagemRepository licencaRepository;
    private final ProcessarVendaFoto processarVendaFoto;
    private final UploadIndexacaoEmLote uploadIndexacaoEmLote;

    public MarketplaceController(JpaFotografoRepository fotografoRepository,
                                  JpaLoteRepository loteRepository,
                                  JpaFotoRepository fotoRepository,
                                  JpaLicencaDeImagemRepository licencaRepository,
                                  ProcessarVendaFoto processarVendaFoto,
                                  UploadIndexacaoEmLote uploadIndexacaoEmLote) {
        this.fotografoRepository = fotografoRepository;
        this.loteRepository = loteRepository;
        this.fotoRepository = fotoRepository;
        this.licencaRepository = licencaRepository;
        this.processarVendaFoto = processarVendaFoto;
        this.uploadIndexacaoEmLote = uploadIndexacaoEmLote;
    }

    @PostMapping("/fotografos")
    public ResponseEntity<Fotografo> criarFotografo(@RequestBody Map<String, String> body) {
        Fotografo fotografo = new Fotografo(body.get("nome"), body.get("email"));
        return ResponseEntity.ok(fotografoRepository.save(fotografo));
    }

    @GetMapping("/fotografos/{id}")
    public ResponseEntity<Fotografo> buscarFotografo(@PathVariable Long id) {
        return fotografoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fotografos/{fotografoId}/lotes")
    public ResponseEntity<Lote> criarLote(@PathVariable Long fotografoId,
                                           @RequestBody Map<String, Long> body) {
        Fotografo fotografo = fotografoRepository.findById(fotografoId)
                .orElseThrow(() -> new IllegalArgumentException("Fotografo nao encontrado"));
        Lote lote = new Lote(body.get("sessionId"), body.get("spotId"), fotografo);
        return ResponseEntity.ok(loteRepository.save(lote));
    }

    @GetMapping("/fotografos/{fotografoId}/lotes")
    public ResponseEntity<List<Lote>> listarLotes(@PathVariable Long fotografoId) {
        return ResponseEntity.ok(loteRepository.findByFotografoId(fotografoId));
    }

    @PostMapping("/lotes/{loteId}/upload")
    public ResponseEntity<Map<String, String>> uploadFotos(@PathVariable Long loteId,
                                                            @RequestBody Map<String, List<String>> body) {
        List<String> caminhos = body.get("caminhos");
        uploadIndexacaoEmLote.executar(loteId, caminhos);
        return ResponseEntity.ok(Map.of("mensagem", "Upload realizado com sucesso",
                "totalFotos", String.valueOf(caminhos.size())));
    }

    @GetMapping("/lotes/{loteId}/fotos")
    public ResponseEntity<List<Foto>> listarFotos(@PathVariable Long loteId) {
        return ResponseEntity.ok(fotoRepository.findByLoteId(loteId));
    }

    @PostMapping("/fotos/{fotoId}/comprar")
    public ResponseEntity<Map<String, String>> comprarLicenca(@PathVariable Long fotoId,
                                                               @RequestBody Map<String, Long> body) {
        try {
            processarVendaFoto.executar(body.get("atletaId"), fotoId);
            return ResponseEntity.ok(Map.of("mensagem", "Licenca adquirida com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/atletas/{atletaId}/licencas")
    public ResponseEntity<List<LicencaDeImagem>> listarLicencas(@PathVariable Long atletaId) {
        return ResponseEntity.ok(licencaRepository.findByAtletaId(atletaId));
    }

    @GetMapping("/fotos/{fotoId}/licencas")
    public ResponseEntity<List<LicencaDeImagem>> listarLicencasPorFoto(@PathVariable Long fotoId) {
        return ResponseEntity.ok(licencaRepository.findByFotoId(fotoId));
    }
}

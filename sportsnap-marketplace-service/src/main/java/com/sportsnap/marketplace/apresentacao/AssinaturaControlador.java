package com.sportsnap.marketplace.apresentacao;

import com.sportsnap.marketplace.dominio.assinatura.Assinatura;
import com.sportsnap.marketplace.dominio.assinatura.AssinaturaServico;
import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assinaturas")
@CrossOrigin(origins = "*")
public class AssinaturaControlador {

    private final AssinaturaServico assinaturaServico;

    public AssinaturaControlador(AssinaturaServico assinaturaServico) {
        this.assinaturaServico = assinaturaServico;
    }

    @PostMapping("/{atletaId}/assinar")
    public ResponseEntity<AssinaturaDto> assinar(@PathVariable Integer atletaId) {
        try {
            Assinatura assinatura = assinaturaServico.assinar(new AtletaId(atletaId));
            return ResponseEntity.ok(toDto(assinatura));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{atletaId}")
    public ResponseEntity<AssinaturaDto> obter(@PathVariable Integer atletaId) {
        try {
            Assinatura assinatura = assinaturaServico.obterAtiva(new AtletaId(atletaId));
            return ResponseEntity.ok(toDto(assinatura));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{atletaId}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Integer atletaId) {
        try {
            assinaturaServico.cancelar(new AtletaId(atletaId));
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Método para os testes de fechamento de ciclo foi removido conforme solicitado

    private AssinaturaDto toDto(Assinatura a) {
        return new AssinaturaDto(
            a.getId().getId(),
            a.getAtletaId().getId(),
            a.getSaldoCotas(),
            a.getDataFimCiclo().toString(),
            a.getStatus().name()
        );
    }

    public record AssinaturaDto(
        String id,
        Integer atletaId,
        int saldoCotas,
        String dataFimCiclo,
        String status
    ) {}
}

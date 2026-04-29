package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.RegistroDeAtividade;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import com.sportsnap.session.domain.repositories.RegistroDeAtividadeRepository;
import com.sportsnap.session.domain.usecases.RegistrarAtividade;
import org.springframework.stereotype.Service;

@Service
public class RegistrarAtividadeImpl implements RegistrarAtividade {

    private final CheckInRepository checkInRepository;
    private final RegistroDeAtividadeRepository registroDeAtividadeRepository;

    public RegistrarAtividadeImpl(CheckInRepository checkInRepository,
                                   RegistroDeAtividadeRepository registroDeAtividadeRepository) {
        this.checkInRepository = checkInRepository;
        this.registroDeAtividadeRepository = registroDeAtividadeRepository;
    }

    @Override
    public void executar(Long checkInId, Double distancia, Integer duracaoSegundos, String intensidade) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("CheckIn nao encontrado: " + checkInId));

        RegistroDeAtividade registro = new RegistroDeAtividade(
                distancia, duracaoSegundos.longValue(), intensidade, checkIn);

        int multiplicador = switch (intensidade.toLowerCase()) {
            case "alta" -> 3;
            case "media" -> 2;
            case "baixa" -> 1;
            default -> 1;
        };

        double xp = distancia * multiplicador;
        registro.setXpCalculado(xp);

        registroDeAtividadeRepository.save(registro);
    }
}

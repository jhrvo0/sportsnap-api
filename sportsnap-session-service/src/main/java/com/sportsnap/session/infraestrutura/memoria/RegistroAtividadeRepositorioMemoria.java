package com.sportsnap.session.infraestrutura.memoria;

import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeId;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeRepositorio;
import com.sportsnap.session.dominio.checkin.CheckInId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class RegistroAtividadeRepositorioMemoria implements RegistroAtividadeRepositorio {

    private final Map<Integer, RegistroAtividade> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public RegistroAtividade salvar(RegistroAtividade registro) {
        RegistroAtividadeId id = registro.getId();
        if (id == null) {
            int novoId = sequencia.getAndIncrement();
            var novo = new RegistroAtividade(new RegistroAtividadeId(novoId),
                                              registro.getCheckInId(),
                                              registro.getDistancia(),
                                              registro.getDuracaoSegundos(),
                                              registro.getIntensidade(),
                                              registro.getXpCalculado());
            armazem.put(novoId, novo);
            return novo;
        }
        armazem.put(id.getId(), registro);
        return registro;
    }

    @Override
    public Optional<RegistroAtividade> obter(RegistroAtividadeId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId) {
        return armazem.values().stream()
            .filter(r -> r.getCheckInId().equals(checkInId))
            .collect(Collectors.toList());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}

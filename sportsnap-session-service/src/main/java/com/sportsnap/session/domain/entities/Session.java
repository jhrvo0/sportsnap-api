package com.sportsnap.session.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Session {

    private Long id;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private Spot spot;
    private List<CheckIn> checkIns = new ArrayList<>();

    public Session() {}

    public Session(LocalDateTime inicio, LocalDateTime fim, Spot spot) {
        this.inicio = inicio;
        this.fim = fim;
        this.spot = spot;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }

    public Spot getSpot() { return spot; }
    public void setSpot(Spot spot) { this.spot = spot; }

    public List<CheckIn> getCheckIns() { return checkIns; }
    public void setCheckIns(List<CheckIn> checkIns) { this.checkIns = checkIns; }
}

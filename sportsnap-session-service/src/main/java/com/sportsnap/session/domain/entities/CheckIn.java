package com.sportsnap.session.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckIn {

    private Long id;
    private Long atletaId;
    private LocalDateTime horario;
    private Double latitude;
    private Double longitude;
    private Session session;
    private List<RegistroDeAtividade> registrosDeAtividade = new ArrayList<>();

    public CheckIn() {}

    public CheckIn(Long atletaId, LocalDateTime horario, Double latitude, Double longitude, Session session) {
        this.atletaId = atletaId;
        this.horario = horario;
        this.latitude = latitude;
        this.longitude = longitude;
        this.session = session;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAtletaId() { return atletaId; }
    public void setAtletaId(Long atletaId) { this.atletaId = atletaId; }

    public LocalDateTime getHorario() { return horario; }
    public void setHorario(LocalDateTime horario) { this.horario = horario; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public List<RegistroDeAtividade> getRegistrosDeAtividade() { return registrosDeAtividade; }
    public void setRegistrosDeAtividade(List<RegistroDeAtividade> registrosDeAtividade) { this.registrosDeAtividade = registrosDeAtividade; }
}

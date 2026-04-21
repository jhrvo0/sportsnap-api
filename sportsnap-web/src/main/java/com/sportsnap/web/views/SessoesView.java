package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

@Route(value = "sessoes", layout = MainLayout.class)
@PageTitle("Sessões")
public class SessoesView extends VerticalLayout {

    public SessoesView(ApiClient apiClient) {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        H2 titulo = new H2("Sessões Disponíveis");

        // Lista de spots
        Grid<Map<String, Object>> spotsGrid = new Grid<>();
        spotsGrid.addColumn(m -> m.get("id")).setHeader("ID");
        spotsGrid.addColumn(m -> m.get("nome")).setHeader("Spot");
        spotsGrid.addColumn(m -> m.get("latitude")).setHeader("Latitude");
        spotsGrid.addColumn(m -> m.get("longitude")).setHeader("Longitude");

        spotsGrid.addComponentColumn(spot -> {
            Button checkInBtn = new Button("Check-in", e -> {
                try {
                    Long spotId = ((Number) spot.get("id")).longValue();
                    List<Map<String, Object>> sessions = apiClient.listarSessions(spotId);
                    if (!sessions.isEmpty()) {
                        Long sessionId = ((Number) sessions.get(0).get("id")).longValue();
                        String lat = String.valueOf(spot.get("latitude"));
                        String lon = String.valueOf(spot.get("longitude"));
                        apiClient.realizarCheckIn(sessionId, userId, lat, lon);
                        Notification.show("Check-in realizado com sucesso!");
                    } else {
                        Notification.show("Nenhuma sessao ativa neste spot");
                    }
                } catch (Exception ex) {
                    Notification.show("Erro: " + ex.getMessage());
                }
            });
            checkInBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return checkInBtn;
        }).setHeader("Ação");

        List<Map<String, Object>> spots = apiClient.listarSpots();
        spotsGrid.setItems(spots);

        add(titulo, spotsGrid);
        setPadding(true);
    }
}

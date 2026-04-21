package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;
import java.util.Map;

@Route(value = "meus-lotes", layout = MainLayout.class)
@PageTitle("Meus Lotes")
public class MeusLotesView extends VerticalLayout {

    public MeusLotesView(ApiClient apiClient) {
        Long userId = (Long) VaadinSession.getCurrent().getAttribute("userId");

        H2 titulo = new H2("Meus Lotes");

        List<Map<String, Object>> lotes = apiClient.listarLotes(userId);

        if (lotes.isEmpty()) {
            add(titulo, new Paragraph("Nenhum lote encontrado. Va para Upload para criar seu primeiro lote."));
        } else {
            Grid<Map<String, Object>> grid = new Grid<>();
            grid.addColumn(m -> m.get("id")).setHeader("ID");
            grid.addColumn(m -> m.get("sessionId")).setHeader("Session");
            grid.addColumn(m -> m.get("spotId")).setHeader("Spot");
            grid.addColumn(m -> m.get("criadoEm")).setHeader("Criado em");

            grid.setItems(lotes);
            add(titulo, grid);
        }

        setPadding(true);
    }
}

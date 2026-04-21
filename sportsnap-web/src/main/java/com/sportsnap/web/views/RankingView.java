package com.sportsnap.web.views;

import com.sportsnap.web.service.ApiClient;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

@Route(value = "ranking", layout = MainLayout.class)
@PageTitle("Ranking")
public class RankingView extends VerticalLayout {

    public RankingView(ApiClient apiClient) {
        H2 titulo = new H2("Ranking");

        Grid<Map<String, Object>> grid = new Grid<>();
        grid.addComponentColumn(m -> {
            int index = grid.getListDataView().getItems().toList().indexOf(m) + 1;
            Span pos = new Span("#" + index);
            if (index == 1) pos.getStyle().set("color", "gold").set("font-weight", "bold");
            else if (index == 2) pos.getStyle().set("color", "silver").set("font-weight", "bold");
            else if (index == 3) pos.getStyle().set("color", "#cd7f32").set("font-weight", "bold");
            return pos;
        }).setHeader("Pos.");

        grid.addColumn(m -> {
            Object atleta = m.get("atleta");
            if (atleta instanceof Map) {
                return ((Map<?, ?>) atleta).get("nome");
            }
            return "Atleta";
        }).setHeader("Atleta");

        grid.addComponentColumn(m -> {
            Object overall = m.get("overall");
            String val = overall != null ? String.valueOf(((Number) overall).intValue()) : "0";
            Span s = new Span(val);
            s.getStyle().set("color", "#00FF7F").set("font-weight", "bold").set("font-size", "18px");
            return s;
        }).setHeader("Overall");

        List<Map<String, Object>> ranking = apiClient.buscarRanking();
        grid.setItems(ranking);

        add(titulo, grid);
        setPadding(true);
    }
}
